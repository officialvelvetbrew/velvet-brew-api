package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.ApiResponse;
import com.cafe.velvetbrew.dto.MenuItemResponse;
import com.cafe.velvetbrew.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/menu")
@RequiredArgsConstructor
public class CustomerMenuController {

    private final MenuItemService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.<List<MenuItemResponse>>builder()
                        .success(true)
                        .message("Menu fetched successfully")
                        .data(menuService.getAll())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getByCategory(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                ApiResponse.<List<MenuItemResponse>>builder()
                        .success(true)
                        .message("Menu fetched successfully")
                        .data(menuService.getByCategory(categoryId))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.<MenuItemResponse>builder()
                        .success(true)
                        .message("Menu item fetched successfully")
                        .data(menuService.getById(id))
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}