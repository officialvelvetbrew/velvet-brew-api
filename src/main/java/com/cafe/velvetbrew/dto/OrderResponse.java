package com.cafe.velvetbrew.dto;


import com.cafe.velvetbrew.common.enums.OrderStatus;
import com.cafe.velvetbrew.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private String orderNumber;

    private String customerName;

    private String mobile;

    private List<OrderItemResponse> items;

    private BigDecimal subtotal;

    private BigDecimal tax;

    private BigDecimal discount;

    private BigDecimal totalAmount;

    private PaymentStatus paymentStatus;

    private OrderStatus orderStatus;

    private String specialInstructions;

    private LocalDateTime orderedAt;

}