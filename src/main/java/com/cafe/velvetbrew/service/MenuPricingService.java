package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.dto.OrderItemRequest;
import com.cafe.velvetbrew.entity.MenuItem;
import com.cafe.velvetbrew.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Prices a cart against live menu data. Shared by OrderServiceImpl (order
 * creation/update) and OfferApplicationServiceImpl (offer preview) so both
 * paths agree on what "unavailable item" and "line total" mean - the two
 * must never independently drift.
 */
@Service
@RequiredArgsConstructor
public class MenuPricingService {

    private final MenuItemRepository menuItemRepository;

    public List<PricedItem> price(List<OrderItemRequest> items) {

        return items.stream().map(itemRequest -> {

            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new RuntimeException(
                            "Menu Item not found : " + itemRequest.getMenuId()));

            if (!Boolean.TRUE.equals(menuItem.getAvailable())) {
                throw new RuntimeException(menuItem.getName() + " is currently unavailable");
            }

            BigDecimal effectivePrice = effectivePriceOf(menuItem);

            BigDecimal lineTotal = effectivePrice
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            return new PricedItem(menuItem, itemRequest.getQuantity(), effectivePrice, lineTotal);

        }).toList();
    }

    /**
     * An active offer price is only honored when it's actually a discount -
     * a null, zero, or higher-than-list offerPrice falls back to list price
     * instead of accidentally raising or zeroing the charge.
     */
    private BigDecimal effectivePriceOf(MenuItem menuItem) {

        BigDecimal offerPrice = menuItem.getOfferPrice();

        if (offerPrice != null
                && offerPrice.compareTo(BigDecimal.ZERO) > 0
                && offerPrice.compareTo(menuItem.getPrice()) < 0) {
            return offerPrice;
        }

        return menuItem.getPrice();
    }

    public BigDecimal subtotalOf(List<PricedItem> pricedItems) {

        return pricedItems.stream()
                .map(PricedItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public record PricedItem(MenuItem menuItem, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }
}
