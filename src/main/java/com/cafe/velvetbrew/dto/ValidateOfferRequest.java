package com.cafe.velvetbrew.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ValidateOfferRequest {

    @NotBlank(message = "Offer code is required")
    private String code;

    /**
     * Optional. Without it, per-customer usage limits can't be checked
     * (the offer may still turn out invalid at actual checkout if a limit
     * is later matched against a resolved customer).
     */
    private String mobile;

    @Valid
    @NotEmpty(message = "Cart cannot be empty")
    private List<OrderItemRequest> items;
}
