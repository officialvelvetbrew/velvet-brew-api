package com.cafe.velvetbrew.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMenuItemRequest {

    @NotNull
    private Long categoryId;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal price;

    private BigDecimal offerPrice;

    private String imageUrl;

    private Boolean veg = true;

    private Boolean available = true;

    private Boolean featured = false;

    private Integer displayOrder = 0;
}