package com.cafe.velvetbrew.service;


import com.cafe.velvetbrew.common.enums.OrderStatus;
import com.cafe.velvetbrew.common.enums.PaymentStatus;
import com.cafe.velvetbrew.common.exception.OrderNotFoundException;
import com.cafe.velvetbrew.dto.CreateOrderRequest;
import com.cafe.velvetbrew.dto.OrderItemResponse;
import com.cafe.velvetbrew.dto.OrderResponse;
import com.cafe.velvetbrew.entity.Customer;
import com.cafe.velvetbrew.entity.Offer;
import com.cafe.velvetbrew.entity.Order;
import com.cafe.velvetbrew.entity.OrderItem;
import com.cafe.velvetbrew.entity.Users;
import com.cafe.velvetbrew.repository.OrderRepository;
import com.cafe.velvetbrew.repository.UserRepository;
import com.cafe.velvetbrew.utils.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final MenuPricingService menuPricingService;
    private final OfferApplicationService offerApplicationService;
    private final UserRepository userRepository;
    private final RecipeInventoryService recipeInventoryService;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {

        Customer customer = customerService.findOrCreate(request.getCustomer());

        Order order = Order.builder()
                .orderNumber(OrderNumberGenerator.generate())
                .customer(customer)
                .user(resolveAuthenticatedUser().orElse(null))
                .paymentStatus(PaymentStatus.PENDING)
                .orderStatus(OrderStatus.PENDING)
                .specialInstructions(request.getSpecialInstructions())
                .build();

        List<MenuPricingService.PricedItem> pricedItems = menuPricingService.price(request.getItems());
        BigDecimal subtotal = menuPricingService.subtotalOf(pricedItems);

        List<OrderItem> orderItems = toOrderItems(order, pricedItems);

        BigDecimal discount = BigDecimal.ZERO;
        Offer appliedOffer = null;

        if (StringUtils.hasText(request.getOfferCode())) {

            OfferApplicationService.OfferDiscount result =
                    offerApplicationService.apply(request.getOfferCode(), subtotal, customer, order.getOrderNumber());

            discount = result.discountAmount();
            appliedOffer = result.offer();
        }

        order.setSubtotal(subtotal);
        order.setTax(BigDecimal.ZERO);
        order.setDiscount(discount);
        order.setTotalAmount(subtotal.subtract(discount));
        order.setOrderItems(orderItems);
        order.setOffer(appliedOffer);

        Order savedOrder = orderRepository.save(order);

        // Deduct recipe ingredients only after the order has an id to
        // reference on the stock movement. Insufficient stock throws and
        // rolls back the whole transaction, so an order is never persisted
        // without the stock to back it. No second save() needed - savedOrder
        // is still managed in this transaction, so JPA's dirty checking
        // persists the flag at commit. (An explicit save() here would call
        // merge() since the id is now non-null, and merge() barfs on the
        // plain immutable List toOrderItems() hands back via Stream.toList().)
        recipeInventoryService.deductForOrder(savedOrder);
        savedOrder.setInventoryDeducted(true);

        log.info("Created order {} for customer {} - {} item(s), subtotal {}, discount {}, total {}",
                savedOrder.getOrderNumber(), customer.getId(), orderItems.size(), subtotal, discount,
                order.getTotalAmount());

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderList() {

        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(String orderNumber, CreateOrderRequest request) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> {
                    log.warn("Order update failed - order {} not found", orderNumber);
                    return new OrderNotFoundException(orderNumber);
                });

        Customer customer = customerService.findOrCreate(request.getCustomer());

        order.setCustomer(customer);
        order.setSpecialInstructions(request.getSpecialInstructions());

        // Release the ingredients consumed for the CURRENT items before they
        // get replaced below - this must run before the collection is
        // cleared, since it reads order.getOrderItems() as they stand now.
        if (Boolean.TRUE.equals(order.getInventoryDeducted())) {
            recipeInventoryService.restockForOrder(order);
        }

        // Remove existing items. orderItems is a cascade=ALL,
        // orphanRemoval=true collection, so the old rows must be deleted by
        // clearing and re-populating this SAME collection instance -
        // replacing it outright with a new List (as this used to do via
        // order.setOrderItems(...) below) breaks Hibernate's ownership
        // tracking for the pending orphan-removal deletes and throws
        // "A collection with cascade=all-delete-orphan was no longer
        // referenced by the owning entity instance" on every edit of an
        // order that already had items.
        order.getOrderItems().clear();

        List<MenuPricingService.PricedItem> pricedItems = menuPricingService.price(request.getItems());
        BigDecimal subtotal = menuPricingService.subtotalOf(pricedItems);

        List<OrderItem> orderItems = toOrderItems(order, pricedItems);

        // An offer already redeemed on this order at creation time has its
        // discount recomputed against the new subtotal (items may have
        // changed), but usage counters/eligibility are not re-checked -
        // that already happened once, at redemption. Applying a NEW offer
        // via update isn't supported: apply() would double-count usage
        // against this same order, so offerCode on an update is ignored.
        BigDecimal discount = order.getOffer() != null
                ? offerApplicationService.recompute(order.getOffer(), subtotal)
                : BigDecimal.ZERO;

        order.setSubtotal(subtotal);
        order.setTax(BigDecimal.ZERO);
        order.setDiscount(discount);
        order.setTotalAmount(subtotal.subtract(discount));
        order.getOrderItems().addAll(orderItems);

        Order updatedOrder = orderRepository.save(order);

        // Deduct ingredients for the NEW items - mirrors the restock above.
        // No second save() needed; see the comment in createOrder().
        recipeInventoryService.deductForOrder(updatedOrder);
        updatedOrder.setInventoryDeducted(true);

        log.info("Updated order {} - {} item(s), subtotal {}, discount {}, total {}",
                orderNumber, orderItems.size(), subtotal, discount, order.getTotalAmount());

        return mapToResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNumber) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new OrderNotFoundException(orderNumber));

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {

        Users user = resolveAuthenticatedUser()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return orderRepository.findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public OrderResponse updateOrderStatus(String orderNumber, OrderStatus orderStatus) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        // Cancelling/rejecting an order releases its recipe ingredients back
        // to stock, since the items were never actually made. Guarded by
        // inventoryDeducted so this can't double-restock (e.g. two status
        // updates to CANCELLED in a row, or an order that never had a
        // matching recipe to begin with).
        boolean releasesInventory = (orderStatus == OrderStatus.CANCELLED || orderStatus == OrderStatus.REJECTED)
                && Boolean.TRUE.equals(order.getInventoryDeducted());

        if (releasesInventory) {
            recipeInventoryService.restockForOrder(order);
            order.setInventoryDeducted(false);
        }

        order.setOrderStatus(orderStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Updated order {} status to {}", orderNumber, orderStatus);

        return mapToResponse(updatedOrder);
    }

    @Override
    public OrderResponse updatePaymentStatus(String orderNumber, PaymentStatus paymentStatus) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        order.setPaymentStatus(paymentStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Updated order {} payment status to {}", orderNumber, paymentStatus);

        return mapToResponse(updatedOrder);
    }

    /**
     * Never null-checked by callers upstream: guest checkout (no bearer
     * token, or an anonymous one) is the common case, so this returns
     * empty rather than throwing - only getMyOrders() treats an absent
     * user as an error, since that endpoint requires a JWT already.
     */
    private Optional<Users> resolveAuthenticatedUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        String identifier = authentication.getName();

        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhoneNumber(identifier));
    }

    private List<OrderItem> toOrderItems(Order order, List<MenuPricingService.PricedItem> pricedItems) {

        // Mutable on purpose - createOrder() assigns this straight onto
        // order.setOrderItems(), and Stream.toList()'s immutable result
        // blows up later with UnsupportedOperationException the moment
        // Hibernate needs to clear/replace the collection (e.g. a second
        // save() on the now-persistent order merges instead of persisting).
        return pricedItems.stream()
                .map(priced -> OrderItem.builder()
                        .order(order)
                        .menuItem(priced.menuItem())
                        .quantity(priced.quantity())
                        .unitPrice(priced.unitPrice())
                        .totalPrice(priced.lineTotal())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items =
                order.getOrderItems()
                        .stream()
                        .map(this::mapItem)
                        .toList();

        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomer().getFullName())
                .mobile(order.getCustomer().getMobile())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .items(items)
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .discount(order.getDiscount())
                .appliedOfferCode(order.getOffer() != null ? order.getOffer().getCode() : null)
                .totalAmount(order.getTotalAmount())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .specialInstructions(order.getSpecialInstructions())
                .orderedAt(order.getCreatedAt())
                .build();
    }

    private OrderItemResponse mapItem(OrderItem item) {

        return OrderItemResponse.builder()
                .menuId(item.getMenuItem().getId())
                .menuName(item.getMenuItem().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

}
