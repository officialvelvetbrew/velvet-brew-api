package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.enums.OrderStatus;
import com.cafe.velvetbrew.dto.CreateOrderRequest;
import com.cafe.velvetbrew.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    List<OrderResponse> getOrderList();

    OrderResponse getOrder(String orderNumber);

    OrderResponse updateOrder(String orderNumber, CreateOrderRequest request);

    // ✅ New method for admin status update
    OrderResponse updateOrderStatus(String orderNumber, OrderStatus orderStatus);
}
