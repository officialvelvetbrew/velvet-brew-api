package com.cafe.velvetbrew.service;

import java.util.List;

import com.cafe.velvetbrew.dto.CreateInventoryItemRequest;
import com.cafe.velvetbrew.dto.InventoryItemResponse;
import com.cafe.velvetbrew.dto.UpdateInventoryItemRequest;

public interface InventoryItemService {

    InventoryItemResponse create(
            CreateInventoryItemRequest request
    );

    List<InventoryItemResponse> getAll();

    InventoryItemResponse getById(Long id);

    InventoryItemResponse update(
            Long id,
            UpdateInventoryItemRequest request
    );

    void delete(Long id);
}
