package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.ApiResponse;
import com.cafe.velvetbrew.service.CacheMaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/cache")
@RequiredArgsConstructor
public class AdminCacheController {

    private final CacheMaintenanceService cacheMaintenanceService;

    /**
     * Clears every Ehcache region immediately - use this right after editing
     * menu/category/offer/inventory/supplier data directly in the database,
     * instead of waiting on the hourly scheduled clear or a redeploy.
     */
    @PostMapping("/clear")
    public ApiResponse<Void> clear() {

        cacheMaintenanceService.clearAll();

        return ApiResponse.success("All caches cleared", null);
    }
}
