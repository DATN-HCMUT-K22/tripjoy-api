package com.tripjoy.api.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Structured address decomposition — mirrors Google Maps API address_components.
 *
 * <p>Google Maps address_component types covered:
 * <ul>
 *   <li>country              → countryName, countryCode (ISO 3166-1 alpha-2)
 *   <li>administrative_area_level_1 → adminArea1 (state/province/tỉnh)
 *   <li>administrative_area_level_2 → adminArea2 (county/district/huyện)
 *   <li>administrative_area_level_3 → adminArea3 (commune/xã)
 *   <li>locality             → city (thành phố/thị xã)
 *   <li>sublocality_level_1  → subLocality (ward/phường/quận)
 *   <li>neighborhood         → neighborhood (khu phố/tổ dân phố)
 *   <li>route                → streetName (tên đường)
 *   <li>street_number        → addressNumber (số nhà)
 *   <li>postal_code          → postcode
 *   <li>plus_code            → plusCode (Google Plus Code, global open location code)
 * </ul>
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressComponents {

    // ==================== Country ====================

    /** Full country name, e.g., "Vietnam", "United States" */
    @Column(name = "country_name", length = 100)
    private String countryName;

    /** ISO 3166-1 alpha-2 country code, e.g., "VN", "US", "TH" */
    @Column(name = "country_code", length = 3)
    private String countryCode;

    // ==================== Administrative Hierarchy ====================

    /**
     * Administrative Area Level 1: largest sub-national division.
     * In Vietnam: Tỉnh/Thành phố (e.g., "Thành phố Hồ Chí Minh").
     * In US: State (e.g., "California").
     * Maps to Google Maps: administrative_area_level_1.
     */
    @Column(name = "admin_area_level1", length = 150)
    private String adminArea1;

    /**
     * Administrative Area Level 1 short name / code.
     * E.g., "HCM", "CA", "BKK".
     */
    @Column(name = "admin_area_level1_code", length = 20)
    private String adminArea1Code;

    /**
     * Administrative Area Level 2: county, district, quận/huyện.
     * In Vietnam: Quận/Huyện (e.g., "Quận 10", "Huyện Bình Chánh").
     * Maps to Google Maps: administrative_area_level_2.
     */
    @Column(name = "admin_area_level2", length = 150)
    private String adminArea2;

    /**
     * Administrative Area Level 3: commune, ward, phường/xã.
     * In Vietnam: Phường/Xã/Thị trấn (e.g., "Phường 14").
     * Maps to Google Maps: administrative_area_level_3.
     */
    @Column(name = "admin_area_level3", length = 150)
    private String adminArea3;

    // ==================== Locality ====================

    /**
     * City/Town/Locality name.
     * For most queries this overlaps with adminArea1 in Vietnam,
     * but differs for cities within provinces (e.g., "Nha Trang" within Khánh Hòa).
     * Maps to Google Maps: locality.
     */
    @Column(name = "city", length = 150)
    private String city;

    /**
     * Sub-locality (ward, phường) within a city.
     * Maps to Google Maps: sublocality / sublocality_level_1.
     */
    @Column(name = "sub_locality", length = 150)
    private String subLocality;

    /**
     * Neighborhood name, e.g., "Khu phố 3", "Historical Quarter".
     * Maps to Google Maps: neighborhood.
     */
    @Column(name = "neighborhood", length = 150)
    private String neighborhood;

    // ==================== Street ====================

    /** Route/Street name, e.g., "Đường Lý Thường Kiệt". Maps to Google Maps: route. */
    @Column(name = "street_name", length = 200)
    private String streetName;

    /** Street/building number, e.g., "268". Maps to Google Maps: street_number. */
    @Column(name = "address_number", length = 30)
    private String addressNumber;

    // ==================== Postal ====================

    /** Postal / ZIP code, e.g., "72500", "10001". Maps to Google Maps: postal_code. */
    @Column(name = "postcode", length = 20)
    private String postcode;

    /**
     * Google Plus Code (Open Location Code) — a short, shareable code for any location on Earth.
     * E.g., "7P28+HM Ho Chi Minh City". Useful for locations without formal addresses.
     * Maps to Google Maps: plus_code.global_code.
     */
    @Column(name = "plus_code", length = 20)
    private String plusCode;

    // ==================== Convenience getters ====================

    /**
     * Returns the most specific administrative area available.
     * Priority: city → adminArea1 → countryName.
     * Useful for display purposes.
     */
    public String getEffectiveCity() {
        if (city != null && !city.isEmpty()) return city;
        if (adminArea1 != null && !adminArea1.isEmpty()) return adminArea1;
        return countryName;
    }

    /**
     * Returns the district (quận/huyện) from the most specific source.
     * Priority: adminArea2 → subLocality.
     */
    public String getEffectiveDistrict() {
        if (adminArea2 != null && !adminArea2.isEmpty()) return adminArea2;
        return subLocality;
    }
}

