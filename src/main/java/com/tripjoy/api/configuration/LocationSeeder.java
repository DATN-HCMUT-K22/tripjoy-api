package com.tripjoy.api.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripjoy.api.entity.Location;
import com.tripjoy.api.entity.embeddable.AddressComponents;
import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;
import com.tripjoy.api.enums.LocationType;
import com.tripjoy.api.enums.MapProvider;
import com.tripjoy.api.enums.OperationalStatus;
import com.tripjoy.api.repository.LocationRepository;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Dynamic Location Seeder — loads administrative location data from JSON config files.
 *
 * <p><b>Design principles:</b>
 * <ul>
 *   <li><b>Idempotent</b>: Safe to re-run on every startup; skips existing records by adminCode+countryCode.
 *   <li><b>Dynamic & extensible</b>: Data lives in {@code resources/seed/locations/} JSON files,
 *       not hardcoded in Java. Add new countries by dropping a new JSON file.
 *   <li><b>Zero external dependency</b>: Seed data is static JSON — no API calls at startup.
 *       Avoids quota costs and startup failures due to network issues.
 *   <li><b>Provider-agnostic</b>: Each record carries optional {@code providerId} (Google place_id)
 *       for later map SDK integration, but it's not required for seeding.
 *   <li><b>Global scale</b>: Supports any country — just add a new JSON seed file.
 * </ul>
 *
 * <p><b>Seed file location:</b> {@code src/main/resources/seed/locations/}
 * File naming convention: {@code {country_code}_{type}.json} (e.g., {@code VN_PROVINCE.json})
 *
 * <p><b>Execution order:</b> @Order(10) — runs after role/permission seeders (which use lower order numbers).
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationSeeder implements ApplicationRunner {

    LocationRepository locationRepository;
    ObjectMapper objectMapper;

    // Seed files to load — add new files here for new countries/types
    static final List<SeedFile> SEED_FILES = List.of(
            new SeedFile("seed/locations/VN_PROVINCE.json", LocationType.PROVINCE, "VN"),
            new SeedFile("seed/locations/VN_COUNTRY.json", LocationType.COUNTRY, "VN")
            // Future: new SeedFile("seed/locations/TH_PROVINCE.json", LocationType.PROVINCE, "TH"),
            // Future: new SeedFile("seed/locations/JP_PROVINCE.json", LocationType.PROVINCE, "JP"),
            );

    static final GeometryFactory GEO_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== LocationSeeder: Starting administrative location seeding ===");

        int totalSeeded = 0;
        int totalSkipped = 0;

        for (SeedFile seedFile : SEED_FILES) {
            SeedResult result = processSeedFile(seedFile);
            totalSeeded += result.seeded();
            totalSkipped += result.skipped();
        }

        log.info("=== LocationSeeder: Done — seeded={}, skipped={} ===", totalSeeded, totalSkipped);
    }

    @Transactional
    protected SeedResult processSeedFile(SeedFile seedFile) {
        ClassPathResource resource = new ClassPathResource(seedFile.path);

        if (!resource.exists()) {
            log.warn("Seed file not found, skipping: {}", seedFile.path);
            return new SeedResult(0, 0);
        }

        List<LocationSeedData> records;
        try (InputStream is = resource.getInputStream()) {
            records = objectMapper.readValue(is, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Failed to read seed file {}: {}", seedFile.path, e.getMessage());
            return new SeedResult(0, 0);
        }

        log.info("Processing seed file: {} ({} records)", seedFile.path, records.size());

        int seeded = 0;
        int skipped = 0;

        for (LocationSeedData data : records) {
            try {
                if (locationRepository.existsByAdminCodeAndCountryCode(data.adminCode, seedFile.countryCode)) {
                    skipped++;
                    continue;
                }

                Location location = buildLocation(data, seedFile.type, seedFile.countryCode);
                locationRepository.save(location);
                seeded++;
                log.debug("Seeded: {} ({})", data.name, data.adminCode);

            } catch (Exception e) {
                log.error("Failed to seed location {} ({}): {}", data.name, data.adminCode, e.getMessage());
            }
        }

        log.info("Seed file {} done — seeded={}, skipped={}", seedFile.path, seeded, skipped);
        return new SeedResult(seeded, skipped);
    }

    private Location buildLocation(LocationSeedData data, LocationType type, String countryCode) {
        Point point = GEO_FACTORY.createPoint(new Coordinate(data.longitude, data.latitude));
        point.setSRID(4326);

        // Build provider ID: prefer the one in data, fallback to stable internal code
        String providerId = (data.providerId != null && !data.providerId.isBlank())
                ? data.providerId
                : countryCode.toLowerCase() + "-" + type.name().toLowerCase() + "-" + data.adminCode;

        return Location.builder()
                .name(data.name)
                .nameEn(data.nameEn)
                .fullAddress(data.name + ", " + data.countryName)
                .placeFormatted(data.name + ", " + data.countryName)
                .latitude(data.latitude)
                .longitude(data.longitude)
                .coordinates(point)
                .viewport(data.viewport)
                .locationType(type)
                .isVerified(true)
                .usageCount(0)
                .provider(MapProvider.MANUAL)
                .providerId(providerId)
                .adminCode(data.adminCode)
                .countryCode(countryCode)
                .timezone(data.timezone)
                .maki(data.maki != null ? data.maki : "city")
                .operationalStatus(OperationalStatus.ACTIVE)
                .softDeleteInfo(new SoftDeleteInfo())
                .addressComponents(AddressComponents.builder()
                        .countryName(data.countryName)
                        .countryCode(countryCode)
                        .adminArea1(data.name)
                        .adminArea1Code(data.adminCode)
                        .city(type == LocationType.PROVINCE ? data.name : null)
                        .build())
                .build();
    }

    // ==================== Inner types (record-style) ====================

    /**
     * Descriptor for a seed file to load.
     * Add new entries to SEED_FILES to load additional countries/types.
     */
    record SeedFile(String path, LocationType type, String countryCode) {}

    record SeedResult(int seeded, int skipped) {}

    /**
     * JSON data model for seed files.
     * Maps directly to the JSON structure in seed/locations/*.json.
     * Using @JsonIgnoreProperties to be forward-compatible with extra fields.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class LocationSeedData {

        /** Admin/province code (e.g., "01" for Hanoi, "79" for HCM). */
        @JsonProperty("admin_code")
        String adminCode;

        /** Vietnamese name, e.g., "Thành phố Hồ Chí Minh". */
        String name;

        /** English name, e.g., "Ho Chi Minh City". */
        @JsonProperty("name_en")
        String nameEn;

        /** Country name in English, e.g., "Vietnam". */
        @JsonProperty("country_name")
        String countryName;

        /** Latitude of the province/city centroid. */
        Double latitude;

        /** Longitude of the province/city centroid. */
        Double longitude;

        /** Google Maps place_id (optional — for SDK integration). */
        @JsonProperty("provider_id")
        String providerId;

        /** IANA timezone string, e.g., "Asia/Ho_Chi_Minh". */
        String timezone;

        /**
         * Recommended map viewport as JSON string.
         * Format: {"northeast":{"lat":x,"lng":y},"southwest":{"lat":x,"lng":y}}
         */
        String viewport;

        /** Maki icon name for map markers. Default "city" if null. */
        String maki;
    }
}
