package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.location.LocationInfoRequest;
import com.tripjoy.api.dto.request.location.LocationRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.location.LocationInfoResponse;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(Endpoint.Location.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Location", description = "Endpoints for managing locations")
public class LocationController {

    LocationService locationService;

    @Operation(summary = "Create a new location")
    @PostMapping
    public ApiResponse<LocationResponse> createLocation(@Valid @RequestBody LocationRequest request) {
        return ApiResponse.<LocationResponse>builder()
//                .data(locationService.createLocation(request))
                .build();
    }

    @Operation(summary = "Get all locations (paginated)")
    @GetMapping
    public ApiResponse<Page<LocationResponse>> getAllLocations(Pageable pageable) {
        return ApiResponse.<Page<LocationResponse>>builder()
//                .data(locationService.getAllLocations(pageable))
                .build();
    }

    @Operation(summary = "Get a single location by ID")
    @GetMapping(Endpoint.Location.ID)
    public ApiResponse<LocationResponse> getLocationById(@PathVariable UUID locationId) {
        return ApiResponse.<LocationResponse>builder()
//                .data(locationService.getLocationById(locationId))
                .build();
    }

    @Operation(summary = "Update a location")
    @PutMapping(Endpoint.Location.ID)
    public ApiResponse<LocationResponse> updateLocation(@PathVariable UUID locationId, @Valid @RequestBody LocationRequest request) {
        return ApiResponse.<LocationResponse>builder()
//                .data(locationService.updateLocation(locationId, request))
                .build();
    }

    @Operation(summary = "Delete a location")
    @DeleteMapping(Endpoint.Location.ID)
    public ApiResponse<Void> deleteLocation(@PathVariable UUID locationId) {
//        locationService.deleteLocation(locationId);
        return ApiResponse.<Void>builder().message("Location deleted successfully").build();
    }


    // --- Location Info (Content) ---

    @Operation(summary = "Get detailed info (content) for a location")
    @GetMapping(Endpoint.Location.ID + "/info")
    public ApiResponse<LocationInfoResponse> getLocationInfo(
            @PathVariable UUID locationId) {

        // return ApiResponse.<LocationInfoResponse>builder()
        //        .data(locationService.getLocationInfo(locationId))
        //        .build();
        return null; // Placeholder
    }

    @Operation(summary = "Update detailed info (content) for a location")
    @PutMapping(Endpoint.Location.ID + "/info")
    public ApiResponse<LocationInfoResponse> updateLocationInfo(
            @PathVariable UUID locationId,
            @Valid @RequestBody LocationInfoRequest request) {

        // return ApiResponse.<LocationInfoResponse>builder()
        //        .data(locationService.updateLocationInfo(locationId, request))
        //        .build();
        return null; // Placeholder
    }
}