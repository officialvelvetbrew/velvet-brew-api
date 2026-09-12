package com.cafe.velvetbrew.repository;


import com.cafe.velvetbrew.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByActiveTrueOrderByDisplayOrderAsc();

    List<MenuItem> findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(Long categoryId);

    List<MenuItem> findByFeaturedTrueAndActiveTrue();

    boolean existsByCategoryIdAndNameIgnoreCase(Long categoryId, String name);

    boolean existsByCategoryId(Long categoryId);
}