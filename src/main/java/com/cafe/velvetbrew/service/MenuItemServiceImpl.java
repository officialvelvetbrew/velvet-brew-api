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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public MenuItemResponse create(CreateMenuItemRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        if (menuRepository.existsByCategoryIdAndNameIgnoreCase(
                request.getCategoryId(),
                request.getName())) {

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

        return map(menuRepository.save(item));
    }

    @Override
    public List<MenuItemResponse> getAll() {
        return menuRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<MenuItemResponse> getByCategory(Long categoryId) {
        return menuRepository.findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(categoryId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public MenuItemResponse getById(Long id) {

        MenuItem item = menuRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Menu item not found."));

        return map(item);
    }

    @Override
    public MenuItemResponse update(Long id, CreateMenuItemRequest request) {

        // We'll implement this next.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void delete(Long id) {

        MenuItem item = menuRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Menu item not found."));

        item.setActive(false);   // Soft delete
        menuRepository.save(item);
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
