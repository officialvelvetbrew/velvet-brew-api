package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.common.enums.DashboardPeriod;
import com.cafe.velvetbrew.dto.BestsellerResponse;
import com.cafe.velvetbrew.dto.DashboardSummaryResponse;
import com.cafe.velvetbrew.dto.LowStockItemResponse;
import com.cafe.velvetbrew.dto.OrderTrendPointResponse;
import com.cafe.velvetbrew.dto.StatusCountResponse;
import com.cafe.velvetbrew.dto.TopCustomerResponse;
import com.cafe.velvetbrew.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return dashboardService.getSummary(from, to);
    }

    @GetMapping("/trend")
    public List<OrderTrendPointResponse> getTrend(
            @RequestParam(defaultValue = "DAY") DashboardPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return dashboardService.getOrderTrend(period, from, to);
    }

    @GetMapping("/bestsellers")
    public List<BestsellerResponse> getBestsellers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {

        return dashboardService.getBestsellers(from, to, limit);
    }

    @GetMapping("/orders/status-breakdown")
    public List<StatusCountResponse> getOrderStatusBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return dashboardService.getOrderStatusBreakdown(from, to);
    }

    @GetMapping("/payments/status-breakdown")
    public List<StatusCountResponse> getPaymentStatusBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return dashboardService.getPaymentStatusBreakdown(from, to);
    }

    @GetMapping("/top-customers")
    public List<TopCustomerResponse> getTopCustomers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {

        return dashboardService.getTopCustomers(from, to, limit);
    }

    @GetMapping("/inventory/low-stock")
    public List<LowStockItemResponse> getLowStockItems() {

        return dashboardService.getLowStockItems();
    }
}
