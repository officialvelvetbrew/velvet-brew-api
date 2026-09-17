package com.cafe.velvetbrew.dto;

import java.math.BigDecimal;

import com.cafe.velvetbrew.common.enums.InventoryUnit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeItemResponse {

	private Long inventoryItemId;
	private String inventoryItemName;
	private InventoryUnit unit;
	private BigDecimal quantity;
}
