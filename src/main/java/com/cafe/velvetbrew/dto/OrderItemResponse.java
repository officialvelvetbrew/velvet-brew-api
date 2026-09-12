package com.cafe.velvetbrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Individual item in an order")
public class OrderItemResponse {

    @Schema(description = "Menu item identifier", example = "12")
    private Long menuId;

    @Schema(description = "Name of the menu item", example = "Cappuccino")
    private String menuName;

    @Schema(description = "Quantity ordered", example = "2")
    private Integer quantity;

    @Schema(description = "Price per unit", example = "180.00")
    private BigDecimal unitPrice;

    @Schema(description = "Total price for this line item (quantity × unitPrice)", example = "360.00")
    private BigDecimal totalPrice;

}