package com.cafe.velvetbrew.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRecipeRequest {
	@Size(max = 150, message = "Recipe name must not exceed 150 characters")
	private String name;

	@Size(max = 255, message = "Description must not exceed 255 characters")
	private String description;

	private Boolean enabled;

	@Valid
	private List<RecipeItemRequest> items;
}
