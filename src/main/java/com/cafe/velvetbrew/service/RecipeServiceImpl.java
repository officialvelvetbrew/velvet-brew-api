package com.cafe.velvetbrew.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafe.velvetbrew.common.exception.ResourceNotFoundException;
import com.cafe.velvetbrew.dto.CreateRecipeRequest;
import com.cafe.velvetbrew.dto.RecipeItemRequest;
import com.cafe.velvetbrew.dto.RecipeResponse;
import com.cafe.velvetbrew.dto.UpdateRecipeRequest;
import com.cafe.velvetbrew.entity.InventoryItem;
import com.cafe.velvetbrew.entity.MenuItem;
import com.cafe.velvetbrew.entity.Recipe;
import com.cafe.velvetbrew.entity.RecipeItem;
import com.cafe.velvetbrew.mapper.RecipeMapper;
import com.cafe.velvetbrew.repository.InventoryItemRepository;
import com.cafe.velvetbrew.repository.MenuItemRepository;
import com.cafe.velvetbrew.repository.RecipeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecipeServiceImpl implements RecipeService {
	private final RecipeRepository recipeRepository;

	private final MenuItemRepository menuItemRepository;

	private final InventoryItemRepository inventoryItemRepository;

	private final RecipeMapper mapper;

	@Override
	public RecipeResponse create(CreateRecipeRequest request) {
		if (recipeRepository.existsByMenuItemId(request.getMenuItemId())) {
			log.warn("Recipe creation rejected - menu item {} already has a recipe", request.getMenuItemId());
			throw new IllegalArgumentException("Menu item already has a recipe: " + request.getMenuItemId());
		}

		MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
				.orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + request.getMenuItemId()));

		Recipe recipe = new Recipe();

		recipe.setMenuItem(menuItem);
		recipe.setName(request.getName().trim());
		recipe.setDescription(request.getDescription());
		recipe.setEnabled(true);
		recipe.setRecipeItems(toRecipeItems(recipe, request.getItems()));

		Recipe saved = recipeRepository.save(recipe);

		log.info("Created recipe id={} for menu item {} with {} ingredient(s)", saved.getId(), menuItem.getId(),
				saved.getRecipeItems().size());

		return mapper.toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RecipeResponse> getAll() {
		return recipeRepository.findAllWithItems().stream().map(mapper::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RecipeResponse getById(Long id) {
		Recipe recipe = recipeRepository.findByIdWithItems(id)
				.orElseThrow(() -> new ResourceNotFoundException("Recipe not found: " + id));

		return mapper.toResponse(recipe);
	}

	@Override
	@Transactional(readOnly = true)
	public RecipeResponse getByMenuItemId(Long menuItemId) {
		Recipe recipe = recipeRepository.findByMenuItemIdWithItems(menuItemId)
				.orElseThrow(() -> new ResourceNotFoundException("Recipe not found for menu item: " + menuItemId));

		return mapper.toResponse(recipe);
	}

	@Override
	public RecipeResponse update(Long id, UpdateRecipeRequest request) {
		Recipe recipe = recipeRepository.findByIdWithItems(id)
				.orElseThrow(() -> new ResourceNotFoundException("Recipe not found: " + id));

		if (request.getName() != null && !request.getName().isBlank()) {
			recipe.setName(request.getName().trim());
		}

		if (request.getDescription() != null) {
			recipe.setDescription(request.getDescription());
		}

		if (request.getEnabled() != null) {
			recipe.setEnabled(request.getEnabled());
		}

		if (request.getItems() != null) {
			if (request.getItems().isEmpty()) {
				throw new IllegalArgumentException("Recipe must have at least one ingredient");
			}

			syncRecipeItems(recipe, request.getItems());
		}

		Recipe updated = recipeRepository.save(recipe);

		log.info("Updated recipe id={}", updated.getId());

		return mapper.toResponse(updated);
	}

	@Override
	public void delete(Long id) {
		Recipe recipe = recipeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Recipe not found: " + id));

		recipeRepository.delete(recipe);

		log.info("Deleted recipe id={}", id);
	}

	private void syncRecipeItems(Recipe recipe, List<RecipeItemRequest> requests) {
		validateNoDuplicates(requests);

		Map<Long, RecipeItemRequest> requested = new HashMap<>();
		requests.forEach(r -> requested.put(r.getInventoryItemId(), r));

		recipe.getRecipeItems().removeIf(item -> !requested.containsKey(item.getInventoryItem().getId()));

		for (RecipeItem existing : recipe.getRecipeItems()) {
			RecipeItemRequest match = requested.remove(existing.getInventoryItem().getId());
			existing.setQuantity(match.getQuantity());
		}

		for (RecipeItemRequest added : requested.values()) {
			recipe.getRecipeItems().add(newRecipeItem(recipe, added));
		}
	}

	private void validateNoDuplicates(List<RecipeItemRequest> requests) {
		long distinct = requests.stream().map(RecipeItemRequest::getInventoryItemId).distinct().count();

		if (distinct != requests.size()) {
			throw new IllegalArgumentException("Recipe cannot reference the same inventory item more than once");
		}
	}

	private RecipeItem newRecipeItem(Recipe recipe, RecipeItemRequest itemRequest) {
		InventoryItem inventoryItem = inventoryItemRepository.findById(itemRequest.getInventoryItemId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Inventory item not found: " + itemRequest.getInventoryItemId()));

		if (!Boolean.TRUE.equals(inventoryItem.getEnabled())) {
			throw new IllegalArgumentException("Inventory item is disabled: " + inventoryItem.getName());
		}

		RecipeItem recipeItem = new RecipeItem();

		recipeItem.setRecipe(recipe);
		recipeItem.setInventoryItem(inventoryItem);
		recipeItem.setQuantity(itemRequest.getQuantity());

		return recipeItem;
	}

	private List<RecipeItem> toRecipeItems(Recipe recipe, List<RecipeItemRequest> requests) {
		validateNoDuplicates(requests);

		return new ArrayList<>(requests.stream().map(r -> newRecipeItem(recipe, r)).toList());
	}
}
