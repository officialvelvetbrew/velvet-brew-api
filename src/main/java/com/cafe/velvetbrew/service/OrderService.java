package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.enums.OrderStatus;
import com.cafe.velvetbrew.common.enums.PaymentStatus;
import com.cafe.velvetbrew.dto.CreateOrderRequest;
import com.cafe.velvetbrew.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);
   List<OrderResponse> getOrderList();
    OrderResponse getOrder(String orderNumber);
    OrderResponse updateOrder(String orderNumber, CreateOrderRequest request);

    OrderResponse updateOrderStatus(String orderNumber, OrderStatus orderStatus);

    /**
     * Manual payment-status override, mainly for cash orders (Razorpay
     * orders already get PaymentStatus.SUCCESS/FAILED set automatically by
     * PaymentServiceImpl on signature verification, so this shouldn't
     * normally be needed there).
     */
    OrderResponse updatePaymentStatus(String orderNumber, PaymentStatus paymentStatus);

    /**
     * Orders tagged to the currently authenticated account (most recent
     * first) - never includes guest-checkout orders placed by anyone else,
     * regardless of matching name/mobile.
     */
    List<OrderResponse> getMyOrders();

}