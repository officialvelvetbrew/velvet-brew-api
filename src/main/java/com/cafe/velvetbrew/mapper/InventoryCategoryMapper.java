package com.cafe.velvetbrew.mapper;

import org.springframework.stereotype.Component;

import com.cafe.velvetbrew.dto.InventoryCategoryResponse;
import com.cafe.velvetbrew.entity.InventoryCategory;

@Component
public class InventoryCategoryMapper {

    public InventoryCategoryResponse toResponse(
            InventoryCategory category) {

        return InventoryCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .enabled(category.getEnabled())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}