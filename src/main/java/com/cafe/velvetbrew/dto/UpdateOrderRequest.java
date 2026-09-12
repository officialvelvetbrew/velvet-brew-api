package com.cafe.velvetbrew.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateOrderRequest {

    private CustomerRequest customer;


    private List<OrderItemRequest> items;

    private String specialInstructions;

    private String orderStatus;

    private String paymentStatus;

}