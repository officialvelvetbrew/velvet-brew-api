package com.cafe.velvetbrew.service;


import com.cafe.velvetbrew.common.exception.MenuItemAlreadyExistsException;
import com.cafe.velvetbrew.common.exception.ResourceNotFoundException;
import com.cafe.velvetbrew.dto.CreateMenuItemRequest;
import com.cafe.velvetbrew.dto.MenuItemResponse;
import com.cafe.velvetbrew.entity.Category;
import com.cafe.velvetbrew.entity.MenuItem;
import com.cafe.velvetbrew.repository.CategoryRepository;
import com.cafe.velvetbrew.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "menuItems", allEntries = true),
            @CacheEvict(value = "menuItemsByCategory", allEntries = true)
    })
    public MenuItemResponse create(CreateMenuItemRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        if (menuRepository.existsByCategoryIdAndNameIgnoreCase(
                request.getCategoryId(),
                request.getName())) {

            log.warn("Menu item creation rejected - duplicate name '{}' in category {}",
                    request.getName(), request.getCategoryId());
            throw new MenuItemAlreadyExistsException(
                    "Menu item already exists.");
        }

        MenuItem item = MenuItem.builder()
                .category(category)
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .offerPrice(request.getOfferPrice())
                .imageUrl(request.getImageUrl())
                .veg(request.getVeg())
                .available(request.getAvailable())
                .featured(request.getFeatured())
                .displayOrder(request.getDisplayOrder())
                .active(true)
                .build();

        MenuItem saved = menuRepository.save(item);

        log.info("Created menu item id={} name={} categoryId={}", saved.getId(), saved.getName(), saved.getCategory().getId());

        return map(saved);
    }

    @Override
    @Cacheable(value = "menuItems", key = "'all'")
    public List<MenuItemResponse> getAll() {
        return menuRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Cacheable(value = "menuItemsByCategory", key = "#categoryId")
    public List<MenuItemResponse> getByCategory(Long categoryId) {
        return menuRepository.findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(categoryId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Cacheable(value = "menuItemById", key = "#id")
    public MenuItemResponse getById(Long id) {

        MenuItem item = menuRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Menu item not found."));

        return map(item);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "menuItems", allEntries = true),
            @CacheEvict(value = "menuItemsByCategory", allEntries = true),
            @CacheEvict(value = "menuItemById", key = "#id")
    })
    public MenuItemResponse update(Long id, CreateMenuItemRequest request) {

        MenuItem item = menuRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Menu item not found."));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        boolean movedOrRenamed =
                !item.getCategory().getId().equals(request.getCategoryId())
                        || !item.getName().equalsIgnoreCase(request.getName());

        if (movedOrRenamed && menuRepository.existsByCategoryIdAndNameIgnoreCase(
                request.getCategoryId(),
                request.getName())) {

            log.warn("Menu item update rejected for id={} - duplicate name '{}' in category {}",
                    id, request.getName(), request.getCategoryId());
            throw new MenuItemAlreadyExistsException(
                    "Menu item already exists.");
        }

        item.setCategory(category);
        item.setName(request.getName().trim());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setOfferPrice(request.getOfferPrice());
        item.setImageUrl(request.getImageUrl());
        item.setVeg(request.getVeg());
        item.setAvailable(request.getAvailable());
        item.setFeatured(request.getFeatured());
        item.setDisplayOrder(request.getDisplayOrder());

        MenuItem updated = menuRepository.save(item);

        log.info("Updated menu item id={}", updated.getId());

        return map(updated);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "menuItems", allEntries = true),
            @CacheEvict(value = "menuItemsByCategory", allEntries = true),
            @CacheEvict(value = "menuItemById", key = "#id")
    })
    public void delete(Long id) {

        MenuItem item = menuRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Menu item not found."));

        item.setActive(false);   // Soft delete
        menuRepository.save(item);

        log.info("Deleted (deactivated) menu item id={} name={}", item.getId(), item.getName());
    }

    private MenuItemResponse map(MenuItem item) {

        return MenuItemResponse.builder()
                .id(item.getId())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .offerPrice(item.getOfferPrice())
                .imageUrl(item.getImageUrl())
                .veg(item.getVeg())
                .available(item.getAvailable())
                .featured(item.getFeatured())
                .build();
    }
}
