package com.cafe.velvetbrew.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreatePaymentResponse {

    private String key;

    private String orderId;

    private BigDecimal amount;

    private String currency;

}