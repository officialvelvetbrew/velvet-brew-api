package com.cafe.velvetbrew.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cafe.velvetbrew.common.enums.InventoryUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory_items", uniqueConstraints = {
		@UniqueConstraint(name = "uk_inventory_items_sku", columnNames = "sku") })
@Getter
@Setter
public class InventoryItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_items_category"))
	private InventoryCategory category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "supplier_id", foreignKey = @ForeignKey(name = "fk_inventory_items_supplier"))
	private Supplier supplier;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, unique = true, length = 50)
	private String sku;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private InventoryUnit unit;

	@Column(name = "current_stock", nullable = false, precision = 15, scale = 3)
	private BigDecimal currentStock = BigDecimal.ZERO;

	@Column(name = "minimum_stock", nullable = false, precision = 15, scale = 3)
	private BigDecimal minimumStock = BigDecimal.ZERO;

	@Column(name = "maximum_stock", precision = 15, scale = 3)
	private BigDecimal maximumStock;

	@Column(name = "reorder_level", nullable = false, precision = 15, scale = 3)
	private BigDecimal reorderLevel = BigDecimal.ZERO;

	@Column(name = "unit_cost", nullable = false, precision = 15, scale = 2)
	private BigDecimal unitCost = BigDecimal.ZERO;

	@Column(nullable = false)
	private Boolean enabled = true;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {

		LocalDateTime now = LocalDateTime.now();

		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {

		updatedAt = LocalDateTime.now();
	}
}