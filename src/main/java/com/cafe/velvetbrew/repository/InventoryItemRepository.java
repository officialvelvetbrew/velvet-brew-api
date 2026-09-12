package com.cafe.velvetbrew.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cafe.velvetbrew.entity.InventoryItem;

import jakarta.persistence.LockModeType;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

	Optional<InventoryItem> findBySkuIgnoreCase(String sku);

	boolean existsBySkuIgnoreCase(String sku);

	List<InventoryItem> findByEnabledTrueOrderByNameAsc();

	List<InventoryItem> findByCategoryId(Long categoryId);

	List<InventoryItem> findBySupplierId(Long supplierId);

	List<InventoryItem> findByCurrentStockLessThanEqual(BigDecimal quantity);

	@Query("""
			SELECT i
			FROM InventoryItem i
			WHERE i.enabled = true AND i.currentStock <= i.reorderLevel
			ORDER BY i.name
			""")
	List<InventoryItem> findLowStockItems();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT i
			FROM InventoryItem i
			WHERE i.id = :id
			""")
	Optional<InventoryItem> findByIdForUpdate(@Param("id") Long id);
}