package com.cafe.velvetbrew.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cafe.velvetbrew.common.enums.StockMovementType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockOperationResponse {

    private Long movementId;

    private Long inventoryItemId;

    private String inventoryItemName;

    private String sku;

    private StockMovementType movementType;

    private BigDecimal quantity;

    private BigDecimal stockBefore;

    private BigDecimal stockAfter;

    private BigDecimal unitCost;

    private String reason;

    private LocalDateTime createdAt;
}