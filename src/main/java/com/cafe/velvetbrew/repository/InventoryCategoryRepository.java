package com.cafe.velvetbrew.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cafe.velvetbrew.entity.InventoryCategory;

public interface InventoryCategoryRepository
        extends JpaRepository<InventoryCategory, Long> {

    Optional<InventoryCategory> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<InventoryCategory> findByEnabledTrueOrderByNameAsc();
}