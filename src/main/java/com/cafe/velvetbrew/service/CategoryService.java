package com.cafe.velvetbrew.service;


import com.cafe.velvetbrew.dto.CategoryResponse;
import com.cafe.velvetbrew.dto.CreateCategoryRequest;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CreateCategoryRequest request);

    List<CategoryResponse> getAll();

    CategoryResponse getById(Long id);

    CategoryResponse update(Long id, CreateCategoryRequest request);

    void delete(Long id);
}