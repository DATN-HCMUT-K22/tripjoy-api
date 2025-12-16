package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.LocationCreateRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.service.ILocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Endpoint.Location.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Location", description = "Endpoints for managing locations (Phase 1 - Core CRUD)")
public class LocationController {

    ILocationService locationService;

    @Operation(summary = "[ADMIN] Create a new location manually", description = "For admin to pre-populate hot/trending locations or batch import. "
            +
            "Normal users should NOT call this - locations are auto-created via suggest flow. " +
            "Automatically prevents duplicates (by providerId or coordinates).")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<LocationResponse> createLocation(@Valid @RequestBody LocationCreateRequest request) {
        return ApiResponse.<LocationResponse>builder()
                .data(locationService.getOrCreateLocation(request))
                .build();
    }

    @Operation(summary = "[ADMIN] Update location info", description = "Only admin can edit shared location data to maintain consistency. "
            +
            "Updates affect all groups suggesting this location.")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(Endpoint.Location.ID)
    public ApiResponse<LocationResponse> updateLocation(@PathVariable UUID locationId,
            @Valid @RequestBody LocationCreateRequest request) {
        return ApiResponse.<LocationResponse>builder()
                .data(locationService.updateLocation(locationId, request))
                .build();
    }

    @Operation(summary = "[ADMIN] Soft delete location", description = "Marks location as deleted (is_deleted=true). Existing trips retain data, "
            +
            "but location won't appear in new searches.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(Endpoint.Location.ID)
    public ApiResponse<Void> deleteLocation(@PathVariable UUID locationId) {
        locationService.deleteLocation(locationId);
        return ApiResponse.<Void>builder().message("Location soft deleted successfully").build();
    }

    @Operation(summary = "Get all locations (paginated)")
    @GetMapping
    public ApiResponse<Page<LocationResponse>> getAllLocations(Pageable pageable) {
        return ApiResponse.<Page<LocationResponse>>builder()
                .data(locationService.getAllLocations(pageable))
                .build();
    }

    @Operation(summary = "Get a single location by ID")
    @GetMapping(Endpoint.Location.ID)
    public ApiResponse<LocationResponse> getLocationById(@PathVariable UUID locationId) {
        return ApiResponse.<LocationResponse>builder()
                .data(locationService.getLocationById(locationId))
                .build();
    }

    @Operation(summary = "Find nearby locations - OK", description = "Search for locations within a specified radius using PostGIS spatial queries. "
            +
            "Results are ordered by distance (nearest first).")
    @GetMapping("/nearby")
    public ApiResponse<List<LocationResponse>> getNearbyLocations(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "5000") Integer radius,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false, defaultValue = "50") Integer limit) {

        List<LocationResponse> nearby = locationService.getNearbyLocations(
                latitude, longitude, radius, categories, limit);

        return ApiResponse.<List<LocationResponse>>builder()
                .data(nearby)
                .build();
    }

    @Operation(summary = "Search locations by text - OK", description = "Search locations by name or address with optional filters for city, district, and categories. "
            +
            "Supports pagination.")
    @GetMapping("/search")
    public ApiResponse<Page<LocationResponse>> searchLocations(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) List<String> categories,
            Pageable pageable) {

        Page<LocationResponse> results = locationService.searchLocations(
                query, city, district, categories, pageable);

        return ApiResponse.<Page<LocationResponse>>builder()
                .data(results)
                .build();
    }
}
