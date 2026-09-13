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
import java.util.List;
import java.util.Optional;

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

        return pricedItems.stream()
                .map(priced -> OrderItem.builder()
                        .order(order)
                        .menuItem(priced.menuItem())
                        .quantity(priced.quantity())
                        .unitPrice(priced.unitPrice())
                        .totalPrice(priced.lineTotal())
                        .build())
                .toList();
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
