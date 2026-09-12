package com.cafe.velvetbrew.dto;

import java.math.BigDecimal;

import com.cafe.velvetbrew.common.enums.StockMovementType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StockAdjustmentRequest {

	@NotNull(message = "Adjustment type is required")
	private StockMovementType type;

	@NotNull(message = "Quantity is required")
	@DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
	private BigDecimal quantity;

	@Size(max = 255, message = "Reason must not exceed 255 characters")
	private String reason;
}