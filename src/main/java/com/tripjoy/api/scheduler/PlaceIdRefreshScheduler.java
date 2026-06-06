package com.tripjoy.api.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.tripjoy.api.entity.Location;
import com.tripjoy.api.repository.LocationRepository;
import com.tripjoy.api.service.IGooglePlacesService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled job to refresh Google Place IDs.
 * Google recommends refreshing Place IDs older than 12 months.
 * If an ID is obsolete (404 NOT_FOUND), it triggers auto-healing via Text Search.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceIdRefreshScheduler {

    private final LocationRepository locationRepository;
    private final IGooglePlacesService googlePlacesService;

    /**
     * Runs at 2:00 AM on the 1st day of every month.
     * Cron expression: "0 0 2 1 * ?"
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void refreshOldPlaceIds() {
        log.info("Starting scheduled job: Refreshing Google Place IDs older than 12 months...");

        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12);
        List<Location> oldLocations = locationRepository.findByUpdatedAtBefore(twelveMonthsAgo);

        if (oldLocations.isEmpty()) {
            log.info("No locations older than 12 months found. Job completed.");
            return;
        }

        log.info("Found {} locations to refresh.", oldLocations.size());
        int updatedCount = 0;
        int healedCount = 0;

        for (Location location : oldLocations) {
            String currentId = location.getProviderId();
            if (currentId == null || currentId.isBlank()) continue;

            try {
                // 1. Attempt free ID refresh
                String newId = googlePlacesService.refreshPlaceId(currentId).block();
                
                if (newId != null && !newId.equals(currentId)) {
                    location.setProviderId(newId);
                    // Explicitly update updatedAt so it doesn't get picked up again for 12 months
                    location.setUpdatedAt(LocalDateTime.now());
                    locationRepository.save(location);
                    updatedCount++;
                    log.info("Refreshed Place ID for '{}': {} -> {}", location.getName(), currentId, newId);
                } else if (newId != null) {
                    // ID is the same, but we still update the timestamp
                    location.setUpdatedAt(LocalDateTime.now());
                    locationRepository.save(location);
                }

            } catch (WebClientResponseException.NotFound e) {
                // 2. Auto-Healing: The ID is obsolete, perform Text Search
                log.warn("Place ID '{}' for '{}' is obsolete (404). Triggering Auto-Healing...", currentId, location.getName());
                try {
                    String healedId = googlePlacesService.findPlaceIdByText(
                            location.getName(), 
                            location.getLatitude(), 
                            location.getLongitude()
                    ).block();

                    if (healedId != null && !healedId.isBlank() && !healedId.equals(currentId)) {
                        location.setProviderId(healedId);
                        location.setUpdatedAt(LocalDateTime.now());
                        locationRepository.save(location);
                        healedCount++;
                        log.info("Auto-Healed Place ID for '{}': {} -> {}", location.getName(), currentId, healedId);
                    } else {
                        log.warn("Auto-Healing failed: Could not find new Place ID for '{}'", location.getName());
                    }
                } catch (Exception ex) {
                    log.error("Error during Auto-Healing for location '{}': {}", location.getName(), ex.getMessage());
                }
            } catch (Exception e) {
                log.error("Error refreshing Place ID for location '{}': {}", location.getName(), e.getMessage());
            }
        }

        log.info("Scheduled job completed. Refreshed: {}, Auto-Healed: {}", updatedCount, healedCount);
    }
}
