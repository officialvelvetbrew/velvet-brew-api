package com.cafe.velvetbrew.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafe.velvetbrew.dto.CreateInventoryItemRequest;
import com.cafe.velvetbrew.dto.InventoryItemResponse;
import com.cafe.velvetbrew.dto.UpdateInventoryItemRequest;
import com.cafe.velvetbrew.entity.InventoryCategory;
import com.cafe.velvetbrew.entity.InventoryItem;
import com.cafe.velvetbrew.entity.Supplier;
import com.cafe.velvetbrew.mapper.InventoryItemMapper;
import com.cafe.velvetbrew.repository.InventoryCategoryRepository;
import com.cafe.velvetbrew.repository.InventoryItemRepository;
import com.cafe.velvetbrew.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryItemServiceImpl implements InventoryItemService {

	private final InventoryItemRepository itemRepository;

	private final InventoryCategoryRepository categoryRepository;

	private final SupplierRepository supplierRepository;

	private final InventoryItemMapper mapper;

	@Override
	public InventoryItemResponse create(CreateInventoryItemRequest request) {

		String sku = request.getSku().trim();

		if (itemRepository.existsBySkuIgnoreCase(sku)) {

			log.warn("Inventory item creation rejected - duplicate SKU: {}", sku);
			throw new IllegalArgumentException("Inventory item with SKU already exists: " + sku);
		}

		InventoryCategory category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new RuntimeException("Inventory category not found: " + request.getCategoryId()));

		Supplier supplier = null;

		if (request.getSupplierId() != null) {

			supplier = supplierRepository.findById(request.getSupplierId())
					.orElseThrow(() -> new RuntimeException("Supplier not found: " + request.getSupplierId()));
		}

		validateStockLevels(request.getMinimumStock(), request.getMaximumStock(), request.getReorderLevel());

		InventoryItem item = new InventoryItem();

		item.setCategory(category);
		item.setSupplier(supplier);

		item.setName(request.getName().trim());

		item.setSku(sku);

		item.setUnit(request.getUnit());

		/*
		 * New inventory item always starts with zero stock.
		 */
		item.setCurrentStock(BigDecimal.ZERO);

		item.setMinimumStock(request.getMinimumStock());

		item.setMaximumStock(request.getMaximumStock());

		item.setReorderLevel(request.getReorderLevel());

		item.setUnitCost(request.getUnitCost());

		item.setEnabled(true);

		InventoryItem saved = itemRepository.save(item);

		log.info("Created inventory item id={} sku={} name={}", saved.getId(), saved.getSku(), saved.getName());

		return mapper.toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<InventoryItemResponse> getAll() {

		return itemRepository.findAll().stream().map(mapper::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public InventoryItemResponse getById(Long id) {

		InventoryItem item = itemRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Inventory item not found: " + id));

		return mapper.toResponse(item);
	}

	@Override
	public InventoryItemResponse update(Long id, UpdateInventoryItemRequest request) {

		InventoryItem item = itemRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Inventory item not found: " + id));

		if (request.getCategoryId() != null) {

			InventoryCategory category = categoryRepository.findById(request.getCategoryId()).orElseThrow(
					() -> new RuntimeException("Inventory category not found: " + request.getCategoryId()));

			item.setCategory(category);
		}

		if (request.getSupplierId() != null) {

			Supplier supplier = supplierRepository.findById(request.getSupplierId())
					.orElseThrow(() -> new RuntimeException("Supplier not found: " + request.getSupplierId()));

			item.setSupplier(supplier);
		}

		if (request.getName() != null && !request.getName().isBlank()) {

			item.setName(request.getName().trim());
		}

		if (request.getSku() != null && !request.getSku().isBlank()) {

			String sku = request.getSku().trim();

			if (!sku.equalsIgnoreCase(item.getSku())) {

				if (itemRepository.existsBySkuIgnoreCase(sku)) {

					throw new IllegalArgumentException("Inventory item with SKU already exists: " + sku);
				}

				item.setSku(sku);
			}
		}

		if (request.getUnit() != null) {
			item.setUnit(request.getUnit());
		}

		if (request.getMinimumStock() != null) {
			item.setMinimumStock(request.getMinimumStock());
		}

		if (request.getMaximumStock() != null) {
			item.setMaximumStock(request.getMaximumStock());
		}

		if (request.getReorderLevel() != null) {
			item.setReorderLevel(request.getReorderLevel());
		}

		if (request.getUnitCost() != null) {
			item.setUnitCost(request.getUnitCost());
		}

		if (request.getEnabled() != null) {
			item.setEnabled(request.getEnabled());
		}

		validateStockLevels(item.getMinimumStock(), item.getMaximumStock(), item.getReorderLevel());

		InventoryItem updated = itemRepository.save(item);

		log.info("Updated inventory item id={} sku={}", updated.getId(), updated.getSku());

		return mapper.toResponse(updated);
	}

	@Override
	public void delete(Long id) {

		InventoryItem item = itemRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Inventory item not found: " + id));

		/*
		 * Soft delete.
		 *
		 * We don't physically delete because stock movements, purchases and recipes can
		 * reference this item.
		 */
		item.setEnabled(false);

		itemRepository.save(item);

		log.info("Deleted (disabled) inventory item id={} sku={}", item.getId(), item.getSku());
	}

	private void validateStockLevels(BigDecimal minimumStock, BigDecimal maximumStock, BigDecimal reorderLevel) {

		if (maximumStock != null && maximumStock.compareTo(minimumStock) < 0) {

			throw new IllegalArgumentException("Maximum stock cannot be less than minimum stock");
		}

		if (maximumStock != null && reorderLevel.compareTo(maximumStock) > 0) {

			throw new IllegalArgumentException("Reorder level cannot be greater than maximum stock");
		}
	}
}