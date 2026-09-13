package com.cafe.velvetbrew.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * A safety net for data changed directly in the database, bypassing the
 * application's own create/update/delete paths (and their @CacheEvict
 * annotations) entirely - e.g. a manual SQL fix. Those changes have no way
 * to invalidate the caches themselves, so this clears everything on a timer
 * to bound how long such an edit can stay invisible, on top of the on-demand
 * admin endpoint for clearing immediately after a known change.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheMaintenanceService {

    private final CacheManager cacheManager;

    @Scheduled(cron = "0 0 * * * *")
    public void clearAllScheduled() {
        clearAll();
    }

    public void clearAll() {

        for (String name : cacheManager.getCacheNames()) {

            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }

        log.info("Cleared all caches: {}", cacheManager.getCacheNames());
    }
}
