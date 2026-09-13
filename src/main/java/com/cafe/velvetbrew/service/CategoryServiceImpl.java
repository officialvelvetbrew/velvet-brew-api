package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.exception.CategoryAlreadyExistsException;
import com.cafe.velvetbrew.common.exception.CategoryInUseException;
import com.cafe.velvetbrew.common.exception.ResourceNotFoundException;
import com.cafe.velvetbrew.dto.CategoryResponse;
import com.cafe.velvetbrew.dto.CreateCategoryRequest;
import com.cafe.velvetbrew.entity.Category;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final MenuItemRepository menuItemRepository;

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse create(CreateCategoryRequest request) {

        if (repository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Category creation rejected - duplicate name: {}", request.getName());
            throw new CategoryAlreadyExistsException("Category already exists.");
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .active(true)
                .build();

        Category saved = repository.save(category);

        log.info("Created category id={} name={}", saved.getId(), saved.getName());

        return map(saved);
    }

    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Cacheable(value = "categoryById", key = "#id")
    public CategoryResponse getById(Long id) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        return map(category);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "categoryById", key = "#id"),
            // Menu item responses embed the category name, so a rename
            // must invalidate them too, not just the category caches.
            @CacheEvict(value = "menuItems", allEntries = true),
            @CacheEvict(value = "menuItemsByCategory", allEntries = true),
            @CacheEvict(value = "menuItemById", allEntries = true)
    })
    public CategoryResponse update(Long id,
                                   CreateCategoryRequest request) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        if (!category.getName().equalsIgnoreCase(request.getName())
                && repository.existsByNameIgnoreCase(request.getName())) {

            log.warn("Category update rejected for id={} - duplicate name: {}", id, request.getName());
            throw new CategoryAlreadyExistsException("Category already exists.");
        }

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());

        Category updated = repository.save(category);

        log.info("Updated category id={} name={}", updated.getId(), updated.getName());

        return map(updated);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "categoryById", key = "#id")
    })
    public void delete(Long id) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        if (menuItemRepository.existsByCategoryId(id)) {
            log.warn("Category deletion rejected for id={} - still has menu items", id);
            throw new CategoryInUseException(
                    "Category cannot be deleted while it still has menu items.");
        }

        repository.delete(category);

        log.info("Deleted category id={} name={}", category.getId(), category.getName());
    }

    private CategoryResponse map(Category category) {

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .build();
    }
}