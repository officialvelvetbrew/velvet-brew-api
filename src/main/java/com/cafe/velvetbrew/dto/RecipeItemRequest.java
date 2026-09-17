package com.cafe.velvetbrew.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecipeItemRequest {

	@NotNull(message = "Inventory item is required")
	private Long inventoryItemId;

	@NotNull(message = "Quantity is required")
	@DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
	private BigDecimal quantity;
}
