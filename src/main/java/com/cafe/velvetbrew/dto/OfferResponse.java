package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.OfferDiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OfferResponse {

    private Long id;

    private String code;

    private String name;

    private String description;

    private OfferDiscountType discountType;

    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minOrderAmount;

    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

    private Integer maxUsesTotal;

    private Integer maxUsesPerCustomer;

    private Integer currentUses;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
