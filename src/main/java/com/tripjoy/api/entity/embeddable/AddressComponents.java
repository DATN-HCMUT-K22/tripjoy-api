package com.tripjoy.api.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressComponents {

    @Column(name = "country_name", length = 100)
    private String countryName; // "Vietnam"

    @Column(name = "country_code", length = 3)
    private String countryCode; // "VN" or "VNM"

    @Column(name = "city", length = 100)
    private String city; // "Ho Chi Minh City"

    @Column(name = "district", length = 100)
    private String district; // "District 1" or "Quan 10"

    @Column(name = "ward", length = 100)
    private String ward; // "Ward 14"

    @Column(name = "street_name", length = 200)
    private String streetName; // "đ. lý thường kiệt"

    @Column(name = "address_number", length = 20)
    private String addressNumber; // "268"

    @Column(name = "postcode", length = 20)
    private String postcode; // "72500"
}
