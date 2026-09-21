package com.cafe.velvetbrew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cafe.velvetbrew.common.exception.ResourceNotFoundException;
import com.cafe.velvetbrew.dto.RecipeItemRequest;
import com.cafe.velvetbrew.dto.RecipeResponse;
import com.cafe.velvetbrew.dto.UpdateRecipeRequest;
import com.cafe.velvetbrew.entity.InventoryItem;
import com.cafe.velvetbrew.entity.MenuItem;
import com.cafe.velvetbrew.entity.Recipe;
import com.cafe.velvetbrew.entity.RecipeItem;
import com.cafe.velvetbrew.mapper.RecipeMapper;
import com.cafe.velvetbrew.repository.InventoryItemRepository;
import com.cafe.velvetbrew.repository.MenuItemRepository;
import com.cafe.velvetbrew.repository.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    private RecipeServiceImpl service;

    private Recipe recipe;

    private InventoryItem beans;

    private InventoryItem milk;

    @BeforeEach
    void setUp() {
        service = new RecipeServiceImpl(recipeRepository, menuItemRepository, inventoryItemRepository,
                new RecipeMapper());

        beans = inventoryItem(1L, "Beans");
        milk = inventoryItem(2L, "Milk");

        recipe = new Recipe();
        recipe.setId(7L);
        recipe.setName("Latte");
        recipe.setEnabled(true);
        MenuItem menuItem = MenuItem.builder().id(43L).name("Latte").build();
        recipe.setMenuItem(menuItem);
        recipe.setRecipeItems(new ArrayList<>(List.of(recipeItem(beans, "18"))));
    }

    private static InventoryItem inventoryItem(Long id, String name) {
        InventoryItem item = new InventoryItem();
        item.setId(id);
        item.setName(name);
        item.setEnabled(true);
        return item;
    }

    private RecipeItem recipeItem(InventoryItem inventoryItem, String quantity) {
        RecipeItem item = new RecipeItem();
        item.setRecipe(recipe);
        item.setInventoryItem(inventoryItem);
        item.setQuantity(new BigDecimal(quantity));
        return item;
    }

    private static RecipeItemRequest request(Long inventoryItemId, String quantity) {
        RecipeItemRequest r = new RecipeItemRequest();
        r.setInventoryItemId(inventoryItemId);
        r.setQuantity(new BigDecimal(quantity));
        return r;
    }

    @Test
    void scenarioE_patchWithTheSameIngredientUpdatesItInPlaceInsteadOfReinserting() {
        RecipeItem original = recipe.getRecipeItems().get(0);
        when(recipeRepository.findByIdWithItems(7L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(recipe)).thenReturn(recipe);

        UpdateRecipeRequest patch = new UpdateRecipeRequest();
        patch.setName("Big Latte");
        patch.setItems(List.of(request(1L, "20")));

        RecipeResponse response = service.update(7L, patch);

        // Same row, new quantity - re-inserting (recipe, beans) was the 500:
        // it violated uk_recipe_items_recipe_inventory before the old row went.
        assertThat(recipe.getRecipeItems()).containsExactly(original);
        assertThat(original.getQuantity()).isEqualByComparingTo("20");
        assertThat(response.getName()).isEqualTo("Big Latte");
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    void scenarioE_patchAddsNewAndRemovesDroppedIngredients() {
        when(recipeRepository.findByIdWithItems(7L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(inventoryItemRepository.findById(2L)).thenReturn(Optional.of(milk));

        UpdateRecipeRequest patch = new UpdateRecipeRequest();
        patch.setItems(List.of(request(2L, "150")));

        service.update(7L, patch);

        assertThat(recipe.getRecipeItems()).hasSize(1);
        assertThat(recipe.getRecipeItems().get(0).getInventoryItem()).isSameAs(milk);
    }

    @Test
    void scenarioE_patchWithoutItemsLeavesIngredientsAlone() {
        when(recipeRepository.findByIdWithItems(7L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(recipe)).thenReturn(recipe);

        UpdateRecipeRequest patch = new UpdateRecipeRequest();
        patch.setEnabled(false);

        service.update(7L, patch);

        assertThat(recipe.getEnabled()).isFalse();
        assertThat(recipe.getRecipeItems()).hasSize(1);
    }

    @Test
    void patchRejectsDuplicateEmptyAndDisabledIngredientsWith400StyleErrors() {
        when(recipeRepository.findByIdWithItems(7L)).thenReturn(Optional.of(recipe));

        UpdateRecipeRequest duplicate = new UpdateRecipeRequest();
        duplicate.setItems(List.of(request(1L, "1"), request(1L, "2")));
        assertThatThrownBy(() -> service.update(7L, duplicate)).isInstanceOf(IllegalArgumentException.class);

        UpdateRecipeRequest empty = new UpdateRecipeRequest();
        empty.setItems(List.of());
        assertThatThrownBy(() -> service.update(7L, empty)).isInstanceOf(IllegalArgumentException.class);

        milk.setEnabled(false);
        when(inventoryItemRepository.findById(2L)).thenReturn(Optional.of(milk));
        UpdateRecipeRequest disabled = new UpdateRecipeRequest();
        disabled.setItems(List.of(request(2L, "1")));
        assertThatThrownBy(() -> service.update(7L, disabled)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void patchOfMissingRecipeOrIngredientIs404NotA500() {
        when(recipeRepository.findByIdWithItems(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, new UpdateRecipeRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        when(recipeRepository.findByIdWithItems(7L)).thenReturn(Optional.of(recipe));
        when(inventoryItemRepository.findById(5L)).thenReturn(Optional.empty());
        UpdateRecipeRequest patch = new UpdateRecipeRequest();
        patch.setItems(List.of(request(5L, "1")));
        assertThatThrownBy(() -> service.update(7L, patch)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void scenarioF_deleteActuallyRemovesTheRecipe() {
        when(recipeRepository.findById(7L)).thenReturn(Optional.of(recipe));

        service.delete(7L);

        verify(recipeRepository).delete(recipe);
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void scenarioF_deletingAMissingRecipeIs404() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
