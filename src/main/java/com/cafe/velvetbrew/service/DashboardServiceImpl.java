package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.enums.DashboardPeriod;
import com.cafe.velvetbrew.dto.BestsellerResponse;
import com.cafe.velvetbrew.dto.DashboardSummaryResponse;
import com.cafe.velvetbrew.dto.LowStockItemResponse;
import com.cafe.velvetbrew.dto.OrderTrendPointResponse;
import com.cafe.velvetbrew.dto.StatusCountResponse;
import com.cafe.velvetbrew.dto.TopCustomerResponse;
import com.cafe.velvetbrew.entity.InventoryItem;
import com.cafe.velvetbrew.repository.InventoryItemRepository;
import com.cafe.velvetbrew.repository.OrderItemRepository;
import com.cafe.velvetbrew.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int DEFAULT_RANGE_DAYS = 30;
    private static final int MAX_LIMIT = 50;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Override
    public DashboardSummaryResponse getSummary(LocalDate from, LocalDate to) {

        LocalDate[] range = resolveRange(from, to);
        LocalDateTime start = range[0].atStartOfDay();
        LocalDateTime end = range[1].plusDays(1).atStartOfDay();

        OrderRepository.OrderSummaryRow row = orderRepository.getSummary(start, end);

        long paidOrders = nullSafe(row.getPaidOrders());
        BigDecimal totalRevenue = row.getTotalRevenue() == null ? BigDecimal.ZERO : row.getTotalRevenue();

        BigDecimal averageOrderValue = paidOrders == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(paidOrders), 2, RoundingMode.HALF_UP);

        return DashboardSummaryResponse.builder()
                .from(range[0])
                .to(range[1])
                .totalOrders(nullSafe(row.getTotalOrders()))
                .paidOrders(paidOrders)
                .completedOrders(nullSafe(row.getCompletedOrders()))
                .cancelledOrders(nullSafe(row.getCancelledOrders()))
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .totalCustomers(nullSafe(row.getTotalCustomers()))
                .build();
    }

    @Override
    public List<OrderTrendPointResponse> getOrderTrend(DashboardPeriod period, LocalDate from, LocalDate to) {

        LocalDate[] range = resolveRange(from, to);
        LocalDateTime start = range[0].atStartOfDay();
        LocalDateTime end = range[1].plusDays(1).atStartOfDay();

        return orderRepository.getTrend(period.getSqlUnit(), start, end)
                .stream()
                .map(row -> OrderTrendPointResponse.builder()
                        .periodStart(row.getPeriodStart().toLocalDate())
                        .orderCount(nullSafe(row.getOrderCount()))
                        .revenue(row.getRevenue() == null ? BigDecimal.ZERO : row.getRevenue())
                        .build())
                .toList();
    }

    @Override
    public List<BestsellerResponse> getBestsellers(LocalDate from, LocalDate to, int limit) {

        LocalDate[] range = resolveRange(from, to);
        LocalDateTime start = range[0].atStartOfDay();
        LocalDateTime end = range[1].plusDays(1).atStartOfDay();

        return orderItemRepository.findBestsellers(start, end, boundedLimit(limit))
                .stream()
                .map(row -> BestsellerResponse.builder()
                        .menuItemId(row.getMenuItemId())
                        .name(row.getName())
                        .categoryName(row.getCategoryName())
                        .quantitySold(nullSafe(row.getQuantitySold()))
                        .revenue(row.getRevenue() == null ? BigDecimal.ZERO : row.getRevenue())
                        .build())
                .toList();
    }

    @Override
    public List<StatusCountResponse> getOrderStatusBreakdown(LocalDate from, LocalDate to) {

        LocalDate[] range = resolveRange(from, to);
        LocalDateTime start = range[0].atStartOfDay();
        LocalDateTime end = range[1].plusDays(1).atStartOfDay();

        return orderRepository.getOrderStatusBreakdown(start, end)
                .stream()
                .map(this::toStatusCount)
                .toList();
    }

    @Override
    public List<StatusCountResponse> getPaymentStatusBreakdown(LocalDate from, LocalDate to) {

        LocalDate[] range = resolveRange(from, to);
        LocalDateTime start = range[0].atStartOfDay();
        LocalDateTime end = range[1].plusDays(1).atStartOfDay();

        return orderRepository.getPaymentStatusBreakdown(start, end)
                .stream()
                .map(this::toStatusCount)
                .toList();
    }

    @Override
    public List<TopCustomerResponse> getTopCustomers(LocalDate from, LocalDate to, int limit) {

        LocalDate[] range = resolveRange(from, to);
        LocalDateTime start = range[0].atStartOfDay();
        LocalDateTime end = range[1].plusDays(1).atStartOfDay();

        return orderRepository.getTopCustomers(start, end, boundedLimit(limit))
                .stream()
                .map(row -> TopCustomerResponse.builder()
                        .customerId(row.getCustomerId())
                        .fullName(row.getFullName())
                        .mobile(row.getMobile())
                        .orderCount(nullSafe(row.getOrderCount()))
                        .totalSpent(row.getTotalSpent() == null ? BigDecimal.ZERO : row.getTotalSpent())
                        .build())
                .toList();
    }

    @Override
    public List<LowStockItemResponse> getLowStockItems() {

        return inventoryItemRepository.findLowStockItems()
                .stream()
                .map(this::toLowStockResponse)
                .toList();
    }

    private StatusCountResponse toStatusCount(OrderRepository.StatusCountRow row) {

        return StatusCountResponse.builder()
                .status(row.getStatus())
                .count(nullSafe(row.getCount()))
                .build();
    }

    private LowStockItemResponse toLowStockResponse(InventoryItem item) {

        return LowStockItemResponse.builder()
                .itemId(item.getId())
                .name(item.getName())
                .sku(item.getSku())
                .currentStock(item.getCurrentStock())
                .reorderLevel(item.getReorderLevel())
                .unit(item.getUnit().name())
                .build();
    }

    private LocalDate[] resolveRange(LocalDate from, LocalDate to) {

        LocalDate resolvedTo = to != null ? to : LocalDate.now();
        LocalDate resolvedFrom = from != null ? from : resolvedTo.minusDays(DEFAULT_RANGE_DAYS - 1);

        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new IllegalArgumentException("'from' date cannot be after 'to' date");
        }

        return new LocalDate[] { resolvedFrom, resolvedTo };
    }

    private int boundedLimit(int limit) {
        return Math.max(1, Math.min(limit, MAX_LIMIT));
    }

    private long nullSafe(Long value) {
        return value == null ? 0 : value;
    }
}
