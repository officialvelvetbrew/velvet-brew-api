package com.cafe.velvetbrew.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafe.velvetbrew.dto.StockOperationRequest;
import com.cafe.velvetbrew.entity.Order;
import com.cafe.velvetbrew.entity.OrderItem;
import com.cafe.velvetbrew.entity.InventoryItem;
import com.cafe.velvetbrew.entity.Recipe;
import com.cafe.velvetbrew.entity.RecipeItem;
import com.cafe.velvetbrew.repository.RecipeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecipeInventoryService {
	private static final String REFERENCE_TYPE = "ORDER";

	private final RecipeRepository recipeRepository;

	private final StockService stockService;

	@Transactional(readOnly = true)
	public void assertStockAvailable(List<OrderItem> orderItems) {
		Map<Long, BigDecimal> required = new LinkedHashMap<>();
		Map<Long, InventoryItem> ingredients = new LinkedHashMap<>();
		Map<Long, Set<String>> usedBy = new LinkedHashMap<>();

		for (OrderItem orderItem : orderItems) {
			Recipe recipe = recipeRepository.findByMenuItemIdWithItems(orderItem.getMenuItem().getId()).orElse(null);

			if (recipe == null || !Boolean.TRUE.equals(recipe.getEnabled())) {
				continue;
			}

			BigDecimal orderedQuantity = BigDecimal.valueOf(orderItem.getQuantity());

			for (RecipeItem recipeItem : recipe.getRecipeItems()) {
				Long id = recipeItem.getInventoryItem().getId();

				ingredients.put(id, recipeItem.getInventoryItem());
				required.merge(id, recipeItem.getQuantity().multiply(orderedQuantity), BigDecimal::add);
				usedBy.computeIfAbsent(id, k -> new LinkedHashSet<>()).add(orderItem.getMenuItem().getName());
			}
		}

		for (Map.Entry<Long, BigDecimal> entry : required.entrySet()) {
			InventoryItem ingredient = ingredients.get(entry.getKey());

			if (!Boolean.TRUE.equals(ingredient.getEnabled())) {
				throw new IllegalStateException(String.format("%s is currently unavailable (ingredient %s is disabled)",
						String.join(", ", usedBy.get(entry.getKey())), ingredient.getName()));
			}

			if (entry.getValue().compareTo(ingredient.getCurrentStock()) > 0) {
				throw new IllegalStateException(String.format(
						"Not enough stock to make %s: %s needs %s %s but only %s %s is available",
						String.join(", ", usedBy.get(entry.getKey())), ingredient.getName(),
						entry.getValue().stripTrailingZeros().toPlainString(), ingredient.getUnit(),
						ingredient.getCurrentStock().stripTrailingZeros().toPlainString(), ingredient.getUnit()));
			}
		}
	}

	public void deductForOrder(Order order) {
		applyToOrder(order, true);
	}

	public void restockForOrder(Order order) {
		applyToOrder(order, false);
	}

	private void applyToOrder(Order order, boolean consume) {
		for (OrderItem orderItem : order.getOrderItems()) {
			Recipe recipe = recipeRepository.findByMenuItemIdWithItems(orderItem.getMenuItem().getId()).orElse(null);

			if (recipe == null || !Boolean.TRUE.equals(recipe.getEnabled())) {
				continue;
			}

			BigDecimal orderedQuantity = BigDecimal.valueOf(orderItem.getQuantity());

			for (RecipeItem recipeItem : recipe.getRecipeItems()) {
				BigDecimal quantity = recipeItem.getQuantity().multiply(orderedQuantity);

				StockOperationRequest request = new StockOperationRequest();

				request.setQuantity(quantity);
				request.setReferenceType(REFERENCE_TYPE);
				request.setReferenceId(order.getId());
				request.setReason((consume ? "Consumed for order " : "Returned for order ") + order.getOrderNumber());

				Long inventoryItemId = recipeItem.getInventoryItem().getId();

				if (consume) {
					stockService.consumeStock(inventoryItemId, request);
				} else {
					stockService.returnStock(inventoryItemId, request);
				}
			}

			log.info("{} recipe stock for order {} - menu item {} x{}", consume ? "Deducted" : "Restocked",
					order.getOrderNumber(), orderItem.getMenuItem().getId(), orderItem.getQuantity());
		}
	}
}
