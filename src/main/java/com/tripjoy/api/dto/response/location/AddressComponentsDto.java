package com.tripjoy.api.dto.response.location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for address components in response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressComponentsDto {

    @Schema(description = "Country name", example = "Vietnam")
    private String countryName;

    @Schema(description = "ISO country code (alpha-2 or alpha-3)", example = "VN")
    private String countryCode;

    @Schema(description = "City/Province name", example = "Hồ Chí Minh")
    private String city;

    @Schema(description = "District name", example = "Quận 10")
    private String district;

    @Schema(description = "Ward/Commune name", example = "Phường 14")
    private String ward;

    @Schema(description = "Street name", example = "đ. lý thường kiệt")
    private String streetName;

    @Schema(description = "Address/building number", example = "268")
    private String addressNumber;

    @Schema(description = "Postal/ZIP code", example = "72500")
    private String postcode;
}
