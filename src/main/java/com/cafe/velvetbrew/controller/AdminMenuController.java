package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.ApiResponse;
import com.cafe.velvetbrew.dto.CreateMenuItemRequest;
import com.cafe.velvetbrew.dto.MenuItemResponse;
import com.cafe.velvetbrew.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/menu")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuItemService menuService;

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponse>> create(
            @Valid @RequestBody CreateMenuItemRequest request) {

        return ResponseEntity.ok(
                ApiResponse.<MenuItemResponse>builder()
                        .success(true)
                        .message("Menu item created successfully")
                        .data(menuService.create(request))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateMenuItemRequest request) {

        return ResponseEntity.ok(
                ApiResponse.<MenuItemResponse>builder()
                        .success(true)
                        .message("Menu item updated successfully")
                        .data(menuService.update(id, request))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        menuService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Menu item deleted successfully")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}