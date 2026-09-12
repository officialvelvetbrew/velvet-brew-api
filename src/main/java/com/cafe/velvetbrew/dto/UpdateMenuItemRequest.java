package com.cafe.velvetbrew.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateMenuItemRequest {



		private Long id;
		private Long categoryId;
		private String name;

		private String description;

		private BigDecimal price;

		private BigDecimal offerPrice;

		private String imageUrl;

		private Boolean veg;

		private Boolean available;

		private Boolean featured;

		private Integer displayOrder;

}
