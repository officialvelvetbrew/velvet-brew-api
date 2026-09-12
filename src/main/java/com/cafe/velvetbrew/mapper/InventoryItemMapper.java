package com.cafe.velvetbrew.mapper;

import org.springframework.stereotype.Component;

import com.cafe.velvetbrew.dto.InventoryItemResponse;
import com.cafe.velvetbrew.entity.InventoryItem;

@Component
public class InventoryItemMapper {

	public InventoryItemResponse toResponse(InventoryItem item) {

		boolean outOfStock = item.getCurrentStock().compareTo(java.math.BigDecimal.ZERO) <= 0;

		boolean lowStock = item.getCurrentStock().compareTo(item.getReorderLevel()) <= 0;

		return InventoryItemResponse.builder()

				.id(item.getId())

				.categoryId(item.getCategory().getId())

				.categoryName(item.getCategory().getName())

				.supplierId(item.getSupplier() != null ? item.getSupplier().getId() : null)

				.supplierName(item.getSupplier() != null ? item.getSupplier().getName() : null)

				.name(item.getName())

				.sku(item.getSku())

				.unit(item.getUnit())

				.currentStock(item.getCurrentStock())

				.minimumStock(item.getMinimumStock())

				.maximumStock(item.getMaximumStock())

				.reorderLevel(item.getReorderLevel())

				.unitCost(item.getUnitCost())

				.lowStock(lowStock)

				.outOfStock(outOfStock)

				.enabled(item.getEnabled())

				.createdAt(item.getCreatedAt())

				.updatedAt(item.getUpdatedAt())

				.build();
	}
}