package com.cafe.velvetbrew.service;


import java.util.List;

import com.cafe.velvetbrew.dto.CreateInventoryCategoryRequest;
import com.cafe.velvetbrew.dto.InventoryCategoryResponse;
import com.cafe.velvetbrew.dto.UpdateInventoryCategoryRequest;

public interface InventoryCategoryService {

    InventoryCategoryResponse create(
            CreateInventoryCategoryRequest request);

    List<InventoryCategoryResponse> getAll();

    InventoryCategoryResponse getById(Long id);

    InventoryCategoryResponse update(
            Long id,
            UpdateInventoryCategoryRequest request);

    void delete(Long id);
}