package com.cafe.velvetbrew.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class OrderTrendPointResponse {

    private LocalDate periodStart;

    private long orderCount;

    private BigDecimal revenue;
}
