package com.tripjoy.api.dto.response.location;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for address components — mirrors Google Maps API address_components structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressComponentsDto {

    @Schema(description = "Country name", example = "Vietnam")
    @JsonProperty("country_name")
    private String countryName;

    @Schema(description = "ISO 3166-1 alpha-2 country code", example = "VN")
    @JsonProperty("country_code")
    private String countryCode;

    @Schema(description = "Administrative area level 1 (state/province/tỉnh)", example = "Thành phố Hồ Chí Minh")
    @JsonProperty("admin_area_level1")
    private String adminArea1;

    @Schema(description = "Admin area level 1 short code", example = "HCM")
    @JsonProperty("admin_area_level1_code")
    private String adminArea1Code;

    @Schema(description = "Administrative area level 2 (district/quận/huyện)", example = "Quận 10")
    @JsonProperty("admin_area_level2")
    private String adminArea2;

    @Schema(description = "Administrative area level 3 (commune/ward/phường/xã)", example = "Phường 14")
    @JsonProperty("admin_area_level3")
    private String adminArea3;

    @Schema(description = "City/locality name (may differ from adminArea1 for cities within provinces)", example = "Nha Trang")
    private String city;

    @Schema(description = "Sub-locality (ward/phường)", example = "Phường Vĩnh Nguyên")
    @JsonProperty("sub_locality")
    private String subLocality;

    @Schema(description = "Neighborhood", example = "Khu phố 3")
    private String neighborhood;

    @Schema(description = "Street/route name", example = "Đường Lý Thường Kiệt")
    @JsonProperty("street_name")
    private String streetName;

    @Schema(description = "Building/street number", example = "268")
    @JsonProperty("address_number")
    private String addressNumber;

    @Schema(description = "Postal/ZIP code", example = "72500")
    private String postcode;

    @Schema(description = "Google Plus Code (Open Location Code)", example = "7P28+HM")
    @JsonProperty("plus_code")
    private String plusCode;
}
