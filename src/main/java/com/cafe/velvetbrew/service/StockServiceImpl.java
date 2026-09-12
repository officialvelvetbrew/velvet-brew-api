package com.cafe.velvetbrew.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafe.velvetbrew.common.enums.StockMovementType;
import com.cafe.velvetbrew.dto.StockAdjustmentRequest;
import com.cafe.velvetbrew.dto.StockMovementResponse;
import com.cafe.velvetbrew.dto.StockOperationRequest;
import com.cafe.velvetbrew.dto.StockOperationResponse;
import com.cafe.velvetbrew.entity.InventoryItem;
import com.cafe.velvetbrew.entity.StockMovement;
import com.cafe.velvetbrew.repository.InventoryItemRepository;
import com.cafe.velvetbrew.repository.StockMovementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

	private final InventoryItemRepository itemRepository;

	private final StockMovementRepository movementRepository;

	@Override
	public StockOperationResponse addStock(Long itemId, StockOperationRequest request) {

		return changeStock(itemId, request.getQuantity(), StockMovementType.PURCHASE, request);
	}

	@Override
	public StockOperationResponse consumeStock(Long itemId, StockOperationRequest request) {

		return changeStock(itemId, request.getQuantity(), StockMovementType.CONSUMPTION, request);
	}

	@Override
	public StockOperationResponse recordWastage(Long itemId, StockOperationRequest request) {

		return changeStock(itemId, request.getQuantity(), StockMovementType.WASTAGE, request);
	}

	@Override
	public StockOperationResponse returnStock(Long itemId, StockOperationRequest request) {

		return changeStock(itemId, request.getQuantity(), StockMovementType.RETURN, request);
	}

	@Override
	public StockOperationResponse adjustStock(Long itemId, StockAdjustmentRequest request) {

		if (request.getType() != StockMovementType.ADJUSTMENT_IN
				&& request.getType() != StockMovementType.ADJUSTMENT_OUT) {

			throw new IllegalArgumentException("Invalid adjustment type");
		}

		StockOperationRequest operation = new StockOperationRequest();

		operation.setQuantity(request.getQuantity());

		operation.setReason(request.getReason());

		return changeStock(itemId, request.getQuantity(), request.getType(), operation);
	}

	private StockOperationResponse changeStock(Long itemId, BigDecimal quantity, StockMovementType type,
			StockOperationRequest request) {

		/*
		 * IMPORTANT: Fetch the inventory row using PESSIMISTIC_WRITE.
		 */
		InventoryItem item = itemRepository.findByIdForUpdate(itemId)
				.orElseThrow(() -> {
					log.warn("Stock operation failed - inventory item {} not found", itemId);
					return new RuntimeException("Inventory item not found: " + itemId);
				});

		if (!Boolean.TRUE.equals(item.getEnabled())) {

			log.warn("Stock operation rejected - item {} ({}) is disabled", item.getId(), item.getName());
			throw new IllegalStateException("Inventory item is disabled");
		}

		BigDecimal before = item.getCurrentStock();

		BigDecimal after;

		boolean increasesStock = type == StockMovementType.PURCHASE || type == StockMovementType.RETURN
				|| type == StockMovementType.ADJUSTMENT_IN;

		if (increasesStock) {

			after = before.add(quantity);

		} else {

			if (quantity.compareTo(before) > 0) {

				log.warn("Stock operation rejected for item {} ({}) - available {}, requested {}",
						item.getId(), item.getName(), before, quantity);
				throw new IllegalStateException(
						"Insufficient stock. " + "Available: " + before + ", Requested: " + quantity);
			}

			after = before.subtract(quantity);
		}

		item.setCurrentStock(after);

		itemRepository.save(item);

		StockMovement movement = new StockMovement();

		movement.setInventoryItem(item);

		movement.setMovementType(type);

		movement.setQuantity(quantity);

		movement.setStockBefore(before);

		movement.setStockAfter(after);

		movement.setUnitCost(request.getUnitCost());

		movement.setReferenceType(request.getReferenceType());

		movement.setReferenceId(request.getReferenceId());

		movement.setReason(request.getReason());

		StockMovement savedMovement = movementRepository.save(movement);

		log.info("Stock {} on item {} ({}): {} -> {} (qty {})",
				type, item.getId(), item.getName(), before, after, quantity);

		return StockOperationResponse.builder()

				.movementId(savedMovement.getId())

				.inventoryItemId(item.getId())

				.inventoryItemName(item.getName())

				.sku(item.getSku())

				.movementType(type)

				.quantity(quantity)

				.stockBefore(before)

				.stockAfter(after)

				.unitCost(request.getUnitCost())

				.reason(request.getReason())

				.createdAt(savedMovement.getCreatedAt())

				.build();
	}

	@Override
	@Transactional(readOnly = true)
	public List<StockMovementResponse> getMovements(Long itemId) {

		/*
		 * Verify item exists.
		 */
		if (!itemRepository.existsById(itemId)) {

			throw new RuntimeException("Inventory item not found: " + itemId);
		}

		return movementRepository.findByInventoryItemIdOrderByCreatedAtDesc(itemId).stream()
				.map(this::toMovementResponse).toList();
	}

	private StockMovementResponse toMovementResponse(StockMovement movement) {

		InventoryItem item = movement.getInventoryItem();

		return StockMovementResponse.builder()

				.id(movement.getId())

				.inventoryItemId(item.getId())

				.inventoryItemName(item.getName())

				.sku(item.getSku())

				.movementType(movement.getMovementType())

				.quantity(movement.getQuantity())

				.stockBefore(movement.getStockBefore())

				.stockAfter(movement.getStockAfter())

				.unitCost(movement.getUnitCost())

				.referenceType(movement.getReferenceType())

				.referenceId(movement.getReferenceId())

				.reason(movement.getReason())

				.createdBy(movement.getCreatedBy())

				.createdAt(movement.getCreatedAt())

				.build();
	}
}