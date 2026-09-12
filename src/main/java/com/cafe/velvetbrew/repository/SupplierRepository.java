package com.cafe.velvetbrew.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cafe.velvetbrew.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

	Optional<Supplier> findByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCase(String name);

	List<Supplier> findByEnabledTrueOrderByNameAsc();
}