package com.cafe.velvetbrew.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cafe.velvetbrew.entity.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
	boolean existsByMenuItemId(Long menuItemId);

	@Query("""
			SELECT DISTINCT r
			FROM Recipe r
			LEFT JOIN FETCH r.recipeItems ri
			LEFT JOIN FETCH ri.inventoryItem
			WHERE r.id = :id
			""")
	Optional<Recipe> findByIdWithItems(@Param("id") Long id);

	@Query("""
			SELECT DISTINCT r
			FROM Recipe r
			LEFT JOIN FETCH r.recipeItems ri
			LEFT JOIN FETCH ri.inventoryItem
			WHERE r.menuItem.id = :menuItemId
			""")
	Optional<Recipe> findByMenuItemIdWithItems(@Param("menuItemId") Long menuItemId);

	@Query("""
			SELECT DISTINCT r
			FROM Recipe r
			LEFT JOIN FETCH r.recipeItems ri
			LEFT JOIN FETCH ri.inventoryItem
			""")
	List<Recipe> findAllWithItems();
}
