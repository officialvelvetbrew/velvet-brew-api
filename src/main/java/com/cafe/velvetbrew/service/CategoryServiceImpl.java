package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.exception.CategoryAlreadyExistsException;
import com.cafe.velvetbrew.common.exception.ResourceNotFoundException;
import com.cafe.velvetbrew.dto.CategoryResponse;
import com.cafe.velvetbrew.dto.CreateCategoryRequest;
import com.cafe.velvetbrew.entity.Category;
import com.cafe.velvetbrew.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    @Override
    public CategoryResponse create(CreateCategoryRequest request) {

        if (repository.existsByNameIgnoreCase(request.getName())) {
            throw new CategoryAlreadyExistsException("Category already exists.");
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .active(true)
                .build();

        return map(repository.save(category));
    }

    @Override
    public List<CategoryResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public CategoryResponse getById(Long id) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        return map(category);
    }

    @Override
    public CategoryResponse update(Long id,
                                   CreateCategoryRequest request) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        if (!category.getName().equalsIgnoreCase(request.getName())
                && repository.existsByNameIgnoreCase(request.getName())) {

            throw new CategoryAlreadyExistsException("Category already exists.");
        }

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());

        return map(repository.save(category));
    }

    @Override
    public void delete(Long id) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        repository.delete(category);
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