package com.cafe.velvetbrew.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DashboardSummaryResponse {

    private LocalDate from;

    private LocalDate to;

    private long totalOrders;

    private long paidOrders;

    private long completedOrders;

    private long cancelledOrders;

    private BigDecimal totalRevenue;

    private BigDecimal averageOrderValue;

    private long totalCustomers;
}
