package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.dto.CreateMenuItemRequest;
import com.cafe.velvetbrew.dto.MenuItemResponse;

import java.util.List;

public interface MenuItemService {

    MenuItemResponse create(CreateMenuItemRequest request);

    List<MenuItemResponse> getAll();

    List<MenuItemResponse> getByCategory(Long categoryId);

    MenuItemResponse getById(Long id);

    MenuItemResponse update(Long id, CreateMenuItemRequest request);

    void delete(Long id);
}