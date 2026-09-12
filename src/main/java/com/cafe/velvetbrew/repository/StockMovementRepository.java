package com.cafe.velvetbrew.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cafe.velvetbrew.entity.InventoryItem;
import com.cafe.velvetbrew.entity.StockMovement;

import jakarta.persistence.LockModeType;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

	List<StockMovement> findByInventoryItemIdOrderByCreatedAtDesc(Long inventoryItemId);

	Page<StockMovement> findByInventoryItemId(Long inventoryItemId, Pageable pageable);

}