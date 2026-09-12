package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.OrderStatus;
import com.cafe.velvetbrew.common.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "Order summary in customer order history context")
public class CustomerOrderHistoryResponse {

    @Schema(description = "Unique order identifier", example = "VB-001234")
    private String orderNumber;

    @Schema(description = "Total order amount after discounts", example = "850.00")
    private BigDecimal totalAmount;

    @Schema(description = "Current status of the order", example = "COMPLETED", enumAsRef = true)
    private OrderStatus orderStatus;

    @Schema(description = "Payment status for the order", example = "SUCCESS", enumAsRef = true)
    private PaymentStatus paymentStatus;

    @Schema(description = "Timestamp when order was placed", example = "2026-09-08T15:30:00")
    private LocalDateTime orderedAt;

    @Schema(description = "List of items in this order")
    private List<OrderItemResponse> items;
}
