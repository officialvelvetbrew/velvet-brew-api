package com.cafe.velvetbrew.dto;

import java.math.BigDecimal;

import com.cafe.velvetbrew.common.enums.InventoryUnit;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateInventoryItemRequest {

	@NotNull(message = "Category is required")
	private Long categoryId;

	private Long supplierId;

	@NotBlank(message = "Item name is required")
	@Size(max = 150, message = "Item name must not exceed 150 characters")
	private String name;

	@NotBlank(message = "SKU is required")
	@Size(max = 50, message = "SKU must not exceed 50 characters")
	private String sku;

	@NotNull(message = "Unit is required")
	private InventoryUnit unit;

	@NotNull(message = "Minimum stock is required")
	@DecimalMin(value = "0.0", message = "Minimum stock cannot be negative")
	private BigDecimal minimumStock;

	@DecimalMin(value = "0.0", message = "Maximum stock cannot be negative")
	private BigDecimal maximumStock;

	@NotNull(message = "Reorder level is required")
	@DecimalMin(value = "0.0", message = "Reorder level cannot be negative")
	private BigDecimal reorderLevel;

	@NotNull(message = "Unit cost is required")
	@DecimalMin(value = "0.0", message = "Unit cost cannot be negative")
	private BigDecimal unitCost;
}