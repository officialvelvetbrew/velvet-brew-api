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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inventory/recipes")
@RequiredArgsConstructor
public class RecipeController {

	private final RecipeService service;

	@PostMapping
	public ResponseEntity<RecipeResponse> create(
			@Valid
			@RequestBody
			CreateRecipeRequest request) {

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(service.create(request));
	}

	@GetMapping
	public ResponseEntity<List<RecipeResponse>> getAll() {

		return ResponseEntity.ok(
				service.getAll()
		);
	}

	@GetMapping("/{id}")
	public ResponseEntity<RecipeResponse> getById(
			@PathVariable Long id) {

		return ResponseEntity.ok(
				service.getById(id)
		);
	}

	@GetMapping("/menu-item/{menuItemId}")
	public ResponseEntity<RecipeResponse> getByMenuItemId(
			@PathVariable Long menuItemId) {

		return ResponseEntity.ok(
				service.getByMenuItemId(menuItemId)
		);
	}

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

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@PathVariable Long id) {

		service.delete(id);

		return ResponseEntity.noContent().build();
	}
}
