package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.dto.CreateOrderRequest;
import com.cafe.velvetbrew.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);
   List<OrderResponse> getOrderList();
    OrderResponse getOrder(String orderNumber);
    OrderResponse updateOrder(String orderNumber, CreateOrderRequest request);

}