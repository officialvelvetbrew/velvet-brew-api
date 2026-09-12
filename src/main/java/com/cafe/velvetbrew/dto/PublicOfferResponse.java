package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.OfferDiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Customer-facing view of an offer - deliberately omits currentUses/
 * maxUsesTotal/maxUsesPerCustomer so a guest can't see internal usage
 * counters or infer how close a promotion is to running out.
 */
@Data
@Builder
public class PublicOfferResponse {

    private String code;

    private String name;

    private String description;

    private OfferDiscountType discountType;

    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minOrderAmount;

    private LocalDateTime endsAt;
}
