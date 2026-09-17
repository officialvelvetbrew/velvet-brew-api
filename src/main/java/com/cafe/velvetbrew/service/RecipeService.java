package com.cafe.velvetbrew.service;

import java.util.List;

import com.cafe.velvetbrew.dto.CreateRecipeRequest;
import com.cafe.velvetbrew.dto.RecipeResponse;
import com.cafe.velvetbrew.dto.UpdateRecipeRequest;

public interface RecipeService {

	RecipeResponse create(CreateRecipeRequest request);

	List<RecipeResponse> getAll();

	RecipeResponse getById(Long id);

	RecipeResponse getByMenuItemId(Long menuItemId);

	RecipeResponse update(Long id, UpdateRecipeRequest request);

	void delete(Long id);
}
