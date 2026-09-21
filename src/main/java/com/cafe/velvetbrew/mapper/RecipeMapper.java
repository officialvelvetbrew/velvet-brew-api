package com.cafe.velvetbrew.mapper;

import org.springframework.stereotype.Component;

import com.cafe.velvetbrew.dto.RecipeItemResponse;
import com.cafe.velvetbrew.dto.RecipeResponse;
import com.cafe.velvetbrew.entity.Recipe;
import com.cafe.velvetbrew.entity.RecipeItem;

@Component
public class RecipeMapper {
	public RecipeResponse toResponse(Recipe recipe) {
		return RecipeResponse.builder()

				.id(recipe.getId())

				.menuItemId(recipe.getMenuItem().getId())

				.menuItemName(recipe.getMenuItem().getName())

				.name(recipe.getName())

				.description(recipe.getDescription())

				.enabled(recipe.getEnabled())

				.items(recipe.getRecipeItems().stream().map(this::toItemResponse).toList())

				.createdAt(recipe.getCreatedAt())

				.updatedAt(recipe.getUpdatedAt())

				.build();
	}

	private RecipeItemResponse toItemResponse(RecipeItem item) {
		return RecipeItemResponse.builder()

				.inventoryItemId(item.getInventoryItem().getId())

				.inventoryItemName(item.getInventoryItem().getName())

				.unit(item.getInventoryItem().getUnit())

				.quantity(item.getQuantity())

				.build();
	}
}
