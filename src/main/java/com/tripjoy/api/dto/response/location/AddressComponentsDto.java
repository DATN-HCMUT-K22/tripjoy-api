package com.tripjoy.api.dto.response.location;

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
    private String countryName;
    private String countryCode;
    private String city;
    private String district;
    private String ward;
    private String streetName;
    private String addressNumber;
    private String postcode;
}
