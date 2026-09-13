package com.cafe.velvetbrew.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafe.velvetbrew.dto.CreateInventoryCategoryRequest;
import com.cafe.velvetbrew.dto.InventoryCategoryResponse;
import com.cafe.velvetbrew.dto.UpdateInventoryCategoryRequest;
import com.cafe.velvetbrew.entity.InventoryCategory;
import com.cafe.velvetbrew.mapper.InventoryCategoryMapper;
import com.cafe.velvetbrew.repository.InventoryCategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryCategoryServiceImpl
        implements InventoryCategoryService {

    private final InventoryCategoryRepository repository;
    private final InventoryCategoryMapper mapper;

    @Override
    @CacheEvict(value = "inventoryCategories", allEntries = true)
    public InventoryCategoryResponse create(
            CreateInventoryCategoryRequest request) {

        if (repository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Inventory category creation rejected - duplicate name: {}", request.getName());
            throw new IllegalArgumentException(
                    "Inventory category already exists: "
                            + request.getName()
            );
        }

        InventoryCategory category = new InventoryCategory();

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setEnabled(true);

        InventoryCategory saved =
                repository.save(category);

        log.info("Created inventory category id={} name={}", saved.getId(), saved.getName());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "inventoryCategories", key = "'all'")
    public List<InventoryCategoryResponse> getAll() {

        return repository
                .findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "inventoryCategoryById", key = "#id")
    public InventoryCategoryResponse getById(Long id) {

        InventoryCategory category =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inventory category not found: "
                                                + id
                                )
                        );

        return mapper.toResponse(category);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "inventoryCategories", allEntries = true),
            @CacheEvict(value = "inventoryCategoryById", key = "#id")
    })
    public InventoryCategoryResponse update(
            Long id,
            UpdateInventoryCategoryRequest request) {

        InventoryCategory category =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inventory category not found: "
                                                + id
                                )
                        );

        if (request.getName() != null
                && !request.getName().isBlank()
                && !request.getName()
                    .equalsIgnoreCase(category.getName())) {

            if (repository.existsByNameIgnoreCase(
                    request.getName())) {

                throw new IllegalArgumentException(
                        "Inventory category already exists: "
                                + request.getName()
                );
            }

            category.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            category.setDescription(
                    request.getDescription()
            );
        }

        if (request.getEnabled() != null) {
            category.setEnabled(
                    request.getEnabled()
            );
        }

        InventoryCategory updated = repository.save(category);

        log.info("Updated inventory category id={} name={}", updated.getId(), updated.getName());

        return mapper.toResponse(updated);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "inventoryCategories", allEntries = true),
            @CacheEvict(value = "inventoryCategoryById", key = "#id")
    })
    public void delete(Long id) {

        InventoryCategory category =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inventory category not found: "
                                                + id
                                )
                        );

        /*
         * Do not physically delete a category that may
         * already be referenced by inventory items.
         *
         * For now, use soft delete.
         */
        category.setEnabled(false);

        repository.save(category);

        log.info("Deleted (disabled) inventory category id={} name={}", category.getId(), category.getName());
    }
}