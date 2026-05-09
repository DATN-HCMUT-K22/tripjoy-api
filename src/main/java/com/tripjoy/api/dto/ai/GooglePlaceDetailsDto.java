package com.tripjoy.api.dto.ai;

import java.util.List;

import lombok.Data;

@Data
public class GooglePlaceDetailsDto {
    private String id;
    private DisplayName displayName;
    private String formattedAddress;
    private Location location;
    private List<String> types;
    private String primaryType;

    @Data
    public static class DisplayName {
        private String text;
    }

    @Data
    public static class Location {
        private Double latitude;
        private Double longitude;
    }
}
