package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.dto.OrderItemRequest;
import com.cafe.velvetbrew.entity.MenuItem;
import com.cafe.velvetbrew.repository.MenuItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuPricingServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private MenuPricingService menuPricingService;

    @Test
    void usesOfferPriceWhenLowerThanListPrice() {

        MenuItem cheeseSandwich = menuItem(1L, "109.00", "1.00");
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(cheeseSandwich));

        List<MenuPricingService.PricedItem> priced = menuPricingService.price(List.of(itemRequest(1L, 2)));

        assertThat(priced).hasSize(1);
        assertThat(priced.get(0).unitPrice()).isEqualByComparingTo("1.00");
        assertThat(priced.get(0).lineTotal()).isEqualByComparingTo("2.00");
        assertThat(menuPricingService.subtotalOf(priced)).isEqualByComparingTo("2.00");
    }

    @Test
    void fallsBackToListPriceWhenNoOfferPrice() {

        MenuItem cappuccino = menuItem(2L, "180.00", null);
        when(menuItemRepository.findById(2L)).thenReturn(Optional.of(cappuccino));

        List<MenuPricingService.PricedItem> priced = menuPricingService.price(List.of(itemRequest(2L, 3)));

        assertThat(priced.get(0).unitPrice()).isEqualByComparingTo("180.00");
        assertThat(priced.get(0).lineTotal()).isEqualByComparingTo("540.00");
    }

    @Test
    void ignoresOfferPriceThatIsNotActuallyADiscount() {

        MenuItem item = menuItem(3L, "100.00", "150.00");
        when(menuItemRepository.findById(3L)).thenReturn(Optional.of(item));

        List<MenuPricingService.PricedItem> priced = menuPricingService.price(List.of(itemRequest(3L, 1)));

        assertThat(priced.get(0).unitPrice()).isEqualByComparingTo("100.00");
    }

    @Test
    void ignoresZeroOfferPrice() {

        MenuItem item = menuItem(4L, "100.00", "0.00");
        when(menuItemRepository.findById(4L)).thenReturn(Optional.of(item));

        List<MenuPricingService.PricedItem> priced = menuPricingService.price(List.of(itemRequest(4L, 1)));

        assertThat(priced.get(0).unitPrice()).isEqualByComparingTo("100.00");
    }

    @Test
    void rejectsUnavailableItem() {

        MenuItem item = menuItem(5L, "100.00", null);
        item.setAvailable(false);
        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> menuPricingService.price(List.of(itemRequest(5L, 1))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("unavailable");
    }

    private MenuItem menuItem(Long id, String price, String offerPrice) {
        return MenuItem.builder()
                .id(id)
                .name("Item " + id)
                .price(new BigDecimal(price))
                .offerPrice(offerPrice == null ? null : new BigDecimal(offerPrice))
                .available(true)
                .build();
    }

    private OrderItemRequest itemRequest(Long menuId, int quantity) {
        OrderItemRequest request = new OrderItemRequest();
        request.setMenuId(menuId);
        request.setQuantity(quantity);
        return request;
    }
}
