package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.OfferDiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateOfferRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Discount type is required")
    private OfferDiscountType discountType;

    @NotNull(message = "Discount value is required")
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
}
