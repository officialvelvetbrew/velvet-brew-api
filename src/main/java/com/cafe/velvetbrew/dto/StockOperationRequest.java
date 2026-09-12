package com.cafe.velvetbrew.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StockOperationRequest {

	@NotNull(message = "Quantity is required")
	@DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
	private BigDecimal quantity;

	@DecimalMin(value = "0.0", message = "Unit cost cannot be negative")
	private BigDecimal unitCost;

	@Size(max = 255, message = "Reason must not exceed 255 characters")
	private String reason;

	private String referenceType;

	private Long referenceId;
}