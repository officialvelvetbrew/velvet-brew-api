package com.cafe.velvetbrew.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRecipeRequest {
	@NotNull(message = "Menu item is required")
	private Long menuItemId;

	@NotBlank(message = "Recipe name is required")
	@Size(max = 150, message = "Recipe name must not exceed 150 characters")
	private String name;

	@Size(max = 255, message = "Description must not exceed 255 characters")
	private String description;

	@NotEmpty(message = "Recipe must have at least one ingredient")
	@Valid
	private List<RecipeItemRequest> items;
}
