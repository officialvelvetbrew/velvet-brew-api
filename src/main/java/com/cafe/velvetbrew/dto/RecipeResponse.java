package com.cafe.velvetbrew.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeResponse {
	private Long id;

	private Long menuItemId;
	private String menuItemName;

	private String name;
	private String description;
	private Boolean enabled;

	private List<RecipeItemResponse> items;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
