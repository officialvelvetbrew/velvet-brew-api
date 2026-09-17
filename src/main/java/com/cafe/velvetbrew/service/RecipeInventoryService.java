package com.cafe.velvetbrew.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafe.velvetbrew.dto.StockOperationRequest;
import com.cafe.velvetbrew.entity.Order;
import com.cafe.velvetbrew.entity.OrderItem;
import com.cafe.velvetbrew.entity.Recipe;
import com.cafe.velvetbrew.entity.RecipeItem;
import com.cafe.velvetbrew.repository.RecipeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bridges order fulfilment to stock movements: a menu item with an enabled
 * recipe consumes/returns its recipe's ingredient quantities (scaled by the
 * order item's quantity) whenever an order is placed, edited or
 * cancelled/rejected. Menu items with no recipe (or a disabled one) are a
 * no-op, so recipes remain opt-in.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecipeInventoryService {

	private static final String REFERENCE_TYPE = "ORDER";

	private final RecipeRepository recipeRepository;

	private final StockService stockService;

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
