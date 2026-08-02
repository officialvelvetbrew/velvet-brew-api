package com.cafe.velvetbrew.service;


import com.cafe.velvetbrew.common.enums.OrderStatus;
import com.cafe.velvetbrew.common.enums.PaymentStatus;
import com.cafe.velvetbrew.common.exception.OrderNotFoundException;
import com.cafe.velvetbrew.dto.CreateOrderRequest;
import com.cafe.velvetbrew.dto.OrderItemRequest;
import com.cafe.velvetbrew.dto.OrderItemResponse;
import com.cafe.velvetbrew.dto.OrderResponse;
import com.cafe.velvetbrew.entity.Customer;
import com.cafe.velvetbrew.entity.MenuItem;
import com.cafe.velvetbrew.entity.Order;
import com.cafe.velvetbrew.entity.OrderItem;
import com.cafe.velvetbrew.repository.MenuItemRepository;
import com.cafe.velvetbrew.repository.OrderRepository;
import com.cafe.velvetbrew.utils.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final CustomerService customerService;



    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {

        Customer customer = customerService.findOrCreate(request.getCustomer());

        Order order = Order.builder()
                .orderNumber(OrderNumberGenerator.generate())
                .customer(customer)
                .paymentStatus(PaymentStatus.PENDING)
                .orderStatus(OrderStatus.PENDING)
                .specialInstructions(request.getSpecialInstructions())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() ->
                            new RuntimeException("Menu Item not found : "
                                    + itemRequest.getMenuId()));

            if (!Boolean.TRUE.equals(menuItem.getAvailable())) {
                throw new RuntimeException(
                        menuItem.getName() + " is currently unavailable");
            }

            BigDecimal lineTotal = menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            subtotal = subtotal.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .totalPrice(lineTotal)
                    .build();

            orderItems.add(orderItem);
        }

        order.setSubtotal(subtotal);
        order.setTax(BigDecimal.ZERO);
        order.setDiscount(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getOrderList() {
        List<OrderResponse> response = new ArrayList<>();
        List<Order> order = orderRepository.findAll();

        order.forEach(x->{
            response.add(mapToResponse(x));
        });

        return response;
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(String orderNumber, CreateOrderRequest request) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new RuntimeException("Order not found : " + orderId));

        Customer customer = customerService.findOrCreate(request.getCustomer());

        order.setCustomer(customer);
        order.setSpecialInstructions(request.getSpecialInstructions());

        // Remove existing items
        order.getOrderItems().clear();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() ->
                            new RuntimeException("Menu Item not found : "
                                    + itemRequest.getMenuId()));

            if (!Boolean.TRUE.equals(menuItem.getAvailable())) {
                throw new RuntimeException(
                        menuItem.getName() + " is currently unavailable");
            }

            BigDecimal lineTotal = menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            subtotal = subtotal.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .totalPrice(lineTotal)
                    .build();

            orderItems.add(orderItem);
        }

        order.setSubtotal(subtotal);
        order.setTax(BigDecimal.ZERO);
        order.setDiscount(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);
        order.setOrderItems(orderItems);

        Order updatedOrder = orderRepository.save(order);

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
                .items(items)
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .discount(order.getDiscount())
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
    
