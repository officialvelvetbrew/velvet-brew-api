package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.OfferDiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * code is intentionally not editable - offer_redemptions and any front-end
 * links to a code should never silently start pointing at different terms.
 * Deactivate and create a new offer instead.
 */
@Data
public class UpdateOfferRequest {

    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private OfferDiscountType discountType;

    @DecimalMin(value = "0.01", message = "Discount value must be greater than zero")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Max discount amount cannot be negative")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
    private BigDecimal minOrderAmount;

    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

    @Min(value = 1, message = "Max total uses must be at least 1")
    private Integer maxUsesTotal;

    @Min(value = 1, message = "Max uses per customer must be at least 1")
    private Integer maxUsesPerCustomer;

    private Boolean active;
}
