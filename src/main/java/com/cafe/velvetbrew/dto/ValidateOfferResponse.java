package com.cafe.velvetbrew.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ValidateOfferResponse {

    private String code;

    private BigDecimal subtotal;

    private BigDecimal discountAmount;

    private BigDecimal finalAmount;
}
