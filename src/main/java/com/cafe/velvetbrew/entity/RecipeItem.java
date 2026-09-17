package com.cafe.velvetbrew.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recipe_items", uniqueConstraints = {
		@UniqueConstraint(name = "uk_recipe_items_recipe_inventory", columnNames = { "recipe_id",
				"inventory_item_id" }) })
@Getter
@Setter
public class RecipeItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recipe_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recipe_items_recipe"))
	private Recipe recipe;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "inventory_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recipe_items_inventory"))
	private InventoryItem inventoryItem;

	@Column(nullable = false, precision = 15, scale = 3)
	private BigDecimal quantity;
}
