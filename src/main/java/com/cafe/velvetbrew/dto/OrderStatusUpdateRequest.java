package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to update order status")
public class OrderStatusUpdateRequest {

    @NotNull(message = "Order status cannot be null")
    @Schema(description = "New order status", example = "ACCEPTED", requiredMode = Schema.RequiredMode.REQUIRED)
    private OrderStatus orderStatus;
}
