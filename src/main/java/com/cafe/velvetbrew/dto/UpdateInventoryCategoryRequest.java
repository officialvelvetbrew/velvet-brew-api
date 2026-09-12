package com.cafe.velvetbrew.dto;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateInventoryCategoryRequest {

    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private Boolean enabled;
}