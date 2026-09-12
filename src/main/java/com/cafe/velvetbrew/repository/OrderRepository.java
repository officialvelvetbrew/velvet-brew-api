package com.cafe.velvetbrew.repository;


import com.cafe.velvetbrew.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<Order> findByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query(value = """
            SELECT
                COUNT(*) AS totalOrders,
                COUNT(*) FILTER (WHERE order_status = 'COMPLETED') AS completedOrders,
                COUNT(*) FILTER (WHERE order_status = 'CANCELLED') AS cancelledOrders,
                COUNT(*) FILTER (WHERE payment_status = 'SUCCESS') AS paidOrders,
                COALESCE(SUM(CASE WHEN payment_status = 'SUCCESS' THEN total_amount ELSE 0 END), 0) AS totalRevenue,
                COUNT(DISTINCT customer_id) AS totalCustomers
            FROM orders
            WHERE created_at >= :from AND created_at < :to
            """, nativeQuery = true)
    OrderSummaryRow getSummary(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT
                date_trunc(:unit, created_at) AS periodStart,
                COUNT(*) AS orderCount,
                COALESCE(SUM(CASE WHEN payment_status = 'SUCCESS' THEN total_amount ELSE 0 END), 0) AS revenue
            FROM orders
            WHERE created_at >= :from AND created_at < :to
            GROUP BY periodStart
            ORDER BY periodStart
            """, nativeQuery = true)
    List<OrderTrendRow> getTrend(@Param("unit") String unit,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT order_status AS status, COUNT(*) AS count
            FROM orders
            WHERE created_at >= :from AND created_at < :to
            GROUP BY order_status
            """, nativeQuery = true)
    List<StatusCountRow> getOrderStatusBreakdown(@Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT payment_status AS status, COUNT(*) AS count
            FROM orders
            WHERE created_at >= :from AND created_at < :to
            GROUP BY payment_status
            """, nativeQuery = true)
    List<StatusCountRow> getPaymentStatusBreakdown(@Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT
                c.id AS customerId,
                c.full_name AS fullName,
                c.mobile AS mobile,
                COUNT(o.id) AS orderCount,
                COALESCE(SUM(o.total_amount), 0) AS totalSpent
            FROM orders o
            JOIN customers c ON c.id = o.customer_id
            WHERE o.payment_status = 'SUCCESS'
              AND o.created_at >= :from AND o.created_at < :to
            GROUP BY c.id, c.full_name, c.mobile
            ORDER BY totalSpent DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TopCustomerRow> getTopCustomers(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          @Param("limit") int limit);

    List<Order> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    @Query(value = """
            SELECT
                COUNT(*) AS totalOrders,
                COALESCE(SUM(total_amount), 0) AS totalSpent,
                MAX(created_at) AS lastOrderDate
            FROM orders
            WHERE customer_id = :customerId
            """, nativeQuery = true)
    CustomerStatsRow getCustomerStats(@Param("customerId") Long customerId);

    @Query(value = """
            SELECT
                COALESCE(SUM(total_amount), 0) AS lifetimeRevenue
            FROM orders
            """, nativeQuery = true)
    GlobalStatsRow getGlobalStats();

    interface OrderSummaryRow {
        Long getTotalOrders();
        Long getCompletedOrders();
        Long getCancelledOrders();
        Long getPaidOrders();
        BigDecimal getTotalRevenue();
        Long getTotalCustomers();
    }

    interface OrderTrendRow {
        LocalDateTime getPeriodStart();
        Long getOrderCount();
        BigDecimal getRevenue();
    }

    interface StatusCountRow {
        String getStatus();
        Long getCount();
    }

    interface TopCustomerRow {
        Long getCustomerId();
        String getFullName();
        String getMobile();
        Long getOrderCount();
        BigDecimal getTotalSpent();
    }

    interface CustomerStatsRow {
        Long getTotalOrders();
        BigDecimal getTotalSpent();
        LocalDateTime getLastOrderDate();
    }

    interface GlobalStatsRow {
        BigDecimal getLifetimeRevenue();
    }
}
