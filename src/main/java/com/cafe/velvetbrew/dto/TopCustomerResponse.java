package com.cafe.velvetbrew.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TopCustomerResponse {

    private Long customerId;

    private String fullName;

    private String mobile;

    private long orderCount;

    private BigDecimal totalSpent;
}
