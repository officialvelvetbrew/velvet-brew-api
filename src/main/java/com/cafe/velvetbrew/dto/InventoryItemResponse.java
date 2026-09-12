package com.cafe.velvetbrew.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cafe.velvetbrew.common.enums.InventoryUnit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryItemResponse {

    private Long id;

    private Long categoryId;
    private String categoryName;

    private Long supplierId;
    private String supplierName;

    private String name;
    private String sku;

    private InventoryUnit unit;

    private BigDecimal currentStock;
    private BigDecimal minimumStock;
    private BigDecimal maximumStock;
    private BigDecimal reorderLevel;
    private BigDecimal unitCost;

    private Boolean lowStock;
    private Boolean outOfStock;

    private Boolean enabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}