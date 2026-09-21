package com.cafe.velvetbrew.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cafe.velvetbrew.dto.CreateRecipeRequest;
import com.cafe.velvetbrew.dto.RecipeResponse;
import com.cafe.velvetbrew.dto.UpdateRecipeRequest;
import com.cafe.velvetbrew.service.RecipeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Recipe Management", description = "Ingredient quantities per menu item (ADMIN/STAFF). Orders consume these from inventory when they reach COMPLETED, and return them if a completed order is later cancelled or rejected.")
@RestController
@RequestMapping("/api/v1/inventory/recipes")
@RequiredArgsConstructor
public class RecipeController {

	private final RecipeService service;

	@Operation(summary = "Create a recipe", description = "One recipe per menu item; each ingredient may appear once.")
	@ApiResponse(responseCode = "201", description = "Created")
	@ApiResponse(responseCode = "400", description = "Validation failed, duplicate ingredient, disabled ingredient, or menu item already has a recipe")
	@ApiResponse(responseCode = "404", description = "Menu item or inventory item not found")
	@PostMapping
	public ResponseEntity<RecipeResponse> create(
			@Valid
			@RequestBody
			CreateRecipeRequest request) {

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(service.create(request));
	}

	@Operation(summary = "List all recipes", description = "Returns every recipe with its ingredients.")
	@ApiResponse(responseCode = "200", description = "OK")
	@GetMapping
	public ResponseEntity<List<RecipeResponse>> getAll() {

		return ResponseEntity.ok(
				service.getAll()
		);
	}

	@Operation(summary = "Get a recipe by id", description = "")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "404", description = "Recipe not found")
	@GetMapping("/{id}")
	public ResponseEntity<RecipeResponse> getById(
			@PathVariable Long id) {

		return ResponseEntity.ok(
				service.getById(id)
		);
	}

	@Operation(summary = "Get the recipe for a menu item", description = "")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "404", description = "No recipe for that menu item")
	@GetMapping("/menu-item/{menuItemId}")
	public ResponseEntity<RecipeResponse> getByMenuItemId(
			@PathVariable Long menuItemId) {

		return ResponseEntity.ok(
				service.getByMenuItemId(menuItemId)
		);
	}

	@Operation(summary = "Update a recipe", description = "Send only what changes. If items is provided it replaces the ingredient list (existing ingredients are updated in place). Set enabled=false to stop a recipe consuming stock without deleting it.")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "400", description = "Validation failed, duplicate or disabled ingredient")
	@ApiResponse(responseCode = "404", description = "Recipe or inventory item not found")
	@PatchMapping("/{id}")
	public ResponseEntity<RecipeResponse> update(
			@PathVariable Long id,

			@Valid
			@RequestBody
			UpdateRecipeRequest request) {

		return ResponseEntity.ok(
				service.update(id, request)
		);
	}

	@Operation(summary = "Delete a recipe", description = "Permanently removes the recipe and its ingredients, freeing the menu item to get a new one. Past stock movements are unaffected.")
	@ApiResponse(responseCode = "204", description = "Deleted")
	@ApiResponse(responseCode = "404", description = "Recipe not found")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@PathVariable Long id) {

		service.delete(id);

		return ResponseEntity.noContent().build();
	}
}
