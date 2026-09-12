package com.cafe.velvetbrew.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LowStockItemResponse {

    private Long itemId;

    private String name;

    private String sku;

    private BigDecimal currentStock;

    private BigDecimal reorderLevel;

    private String unit;
}
