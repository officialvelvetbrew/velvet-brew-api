package com.cafe.velvetbrew.dto;

import java.math.BigDecimal;

import com.cafe.velvetbrew.common.enums.InventoryUnit;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateInventoryItemRequest {

	private Long categoryId;

	private Long supplierId;

	@Size(max = 150, message = "Item name must not exceed 150 characters")
	private String name;

	@Size(max = 50, message = "SKU must not exceed 50 characters")
	private String sku;

	private InventoryUnit unit;

	@DecimalMin(value = "0.0", message = "Minimum stock cannot be negative")
	private BigDecimal minimumStock;

	@DecimalMin(value = "0.0", message = "Maximum stock cannot be negative")
	private BigDecimal maximumStock;

	@DecimalMin(value = "0.0", message = "Reorder level cannot be negative")
	private BigDecimal reorderLevel;

	@DecimalMin(value = "0.0", message = "Unit cost cannot be negative")
	private BigDecimal unitCost;

	private Boolean enabled;
}