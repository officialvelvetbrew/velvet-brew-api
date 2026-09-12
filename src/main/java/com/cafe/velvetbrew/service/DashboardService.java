package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.enums.DashboardPeriod;
import com.cafe.velvetbrew.dto.BestsellerResponse;
import com.cafe.velvetbrew.dto.DashboardSummaryResponse;
import com.cafe.velvetbrew.dto.LowStockItemResponse;
import com.cafe.velvetbrew.dto.OrderTrendPointResponse;
import com.cafe.velvetbrew.dto.StatusCountResponse;
import com.cafe.velvetbrew.dto.TopCustomerResponse;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardSummaryResponse getSummary(LocalDate from, LocalDate to);

    List<OrderTrendPointResponse> getOrderTrend(DashboardPeriod period, LocalDate from, LocalDate to);

    List<BestsellerResponse> getBestsellers(LocalDate from, LocalDate to, int limit);

    List<StatusCountResponse> getOrderStatusBreakdown(LocalDate from, LocalDate to);

    List<StatusCountResponse> getPaymentStatusBreakdown(LocalDate from, LocalDate to);

    List<TopCustomerResponse> getTopCustomers(LocalDate from, LocalDate to, int limit);

    List<LowStockItemResponse> getLowStockItems();
}
