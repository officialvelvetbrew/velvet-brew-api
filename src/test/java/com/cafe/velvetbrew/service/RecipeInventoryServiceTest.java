package com.cafe.velvetbrew.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.cafe.velvetbrew.common.enums.InventoryUnit;
import com.cafe.velvetbrew.entity.InventoryItem;
import com.cafe.velvetbrew.entity.MenuItem;
import com.cafe.velvetbrew.entity.OrderItem;
import com.cafe.velvetbrew.entity.Recipe;
import com.cafe.velvetbrew.entity.RecipeItem;
import com.cafe.velvetbrew.repository.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class RecipeInventoryServiceTest {
    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private StockService stockService;

    private RecipeInventoryService service;

    private InventoryItem beans;

    private MenuItem espresso;

    private MenuItem latte;

    @BeforeEach
    void setUp() {
        service = new RecipeInventoryService(recipeRepository, stockService);

        beans = new InventoryItem();
        beans.setId(1L);
        beans.setName("Beans");
        beans.setUnit(InventoryUnit.G);
        beans.setEnabled(true);
        beans.setCurrentStock(new BigDecimal("100.000"));

        espresso = MenuItem.builder().id(10L).name("Espresso").build();
        latte = MenuItem.builder().id(11L).name("Latte").build();
    }

    private void recipe(MenuItem menuItem, boolean enabled, String gramsOfBeans) {
        Recipe recipe = new Recipe();
        recipe.setMenuItem(menuItem);
        recipe.setEnabled(enabled);
        RecipeItem item = new RecipeItem();
        item.setRecipe(recipe);
        item.setInventoryItem(beans);
        item.setQuantity(new BigDecimal(gramsOfBeans));
        recipe.setRecipeItems(new ArrayList<>(List.of(item)));
        when(recipeRepository.findByMenuItemIdWithItems(menuItem.getId())).thenReturn(Optional.of(recipe));
    }

    private static OrderItem line(MenuItem menuItem, int quantity) {
        return OrderItem.builder().menuItem(menuItem).quantity(quantity).build();
    }

    @Test
    void passesWhenEnoughStockAndAllowsExactlyWhatIsLeft() {
        recipe(espresso, true, "20");

        assertThatCode(() -> service.assertStockAvailable(List.of(line(espresso, 5)))).doesNotThrowAnyException();
    }

    @Test
    void rejectsWhenNotEnoughStockAndSaysWhatIsShort() {
        recipe(espresso, true, "20");

        assertThatThrownBy(() -> service.assertStockAvailable(List.of(line(espresso, 6))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Espresso")
                .hasMessageContaining("Beans needs 120 G")
                .hasMessageContaining("only 100 G");
    }

    @Test
    void sumsTheSameIngredientAcrossDifferentLinesOfOneOrder() {
        recipe(espresso, true, "20");
        recipe(latte, true, "30");

        assertThatThrownBy(() -> service.assertStockAvailable(List.of(line(espresso, 3), line(latte, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Espresso, Latte");
    }

    @Test
    void rejectsWhenAnIngredientIsDisabled() {
        beans.setEnabled(false);
        recipe(espresso, true, "20");

        assertThatThrownBy(() -> service.assertStockAvailable(List.of(line(espresso, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unavailable");
    }

    @Test
    void ignoresItemsWithNoRecipeOrADisabledRecipe() {
        when(recipeRepository.findByMenuItemIdWithItems(10L)).thenReturn(Optional.empty());
        recipe(latte, false, "999");

        assertThatCode(() -> service.assertStockAvailable(List.of(line(espresso, 50), line(latte, 50))))
                .doesNotThrowAnyException();
    }
}
