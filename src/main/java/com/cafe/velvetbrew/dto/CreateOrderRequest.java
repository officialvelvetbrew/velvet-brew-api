package com.cafe.velvetbrew.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @Valid
    private CustomerRequest customer;

    @Valid
    @NotEmpty(message = "Cart cannot be empty")
    private List<OrderItemRequest> items;

    private String specialInstructions;

    /**
     * Optional coupon code. Validated and priced entirely server-side
     * (OfferApplicationService) - never trust a discount amount from the
     * client, same principle as order/payment totals.
     */
    private String offerCode;

}