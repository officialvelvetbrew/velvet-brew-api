package com.cafe.velvetbrew.repository;

import com.cafe.velvetbrew.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query(value = """
            SELECT
                mi.id AS menuItemId,
                mi.name AS name,
                c.name AS categoryName,
                SUM(oi.quantity) AS quantitySold,
                SUM(oi.total_price) AS revenue
            FROM order_items oi
            JOIN orders o ON o.id = oi.order_id
            JOIN menu_items mi ON mi.id = oi.menu_item_id
            JOIN categories c ON c.id = mi.category_id
            WHERE o.order_status <> 'CANCELLED'
              AND o.created_at >= :from AND o.created_at < :to
            GROUP BY mi.id, mi.name, c.name
            ORDER BY quantitySold DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<BestsellerRow> findBestsellers(@Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to,
                                         @Param("limit") int limit);

    interface BestsellerRow {
        Long getMenuItemId();
        String getName();
        String getCategoryName();
        Long getQuantitySold();
        BigDecimal getRevenue();
    }
}
