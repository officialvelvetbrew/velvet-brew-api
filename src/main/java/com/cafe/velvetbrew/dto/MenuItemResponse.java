package com.cafe.velvetbrew.dto;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MenuItemResponse {

    private Long id;

    private Long categoryId;

    private String categoryName;

    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal offerPrice;

    private String imageUrl;

    private Boolean veg;

    private Boolean available;

    private Boolean featured;
}