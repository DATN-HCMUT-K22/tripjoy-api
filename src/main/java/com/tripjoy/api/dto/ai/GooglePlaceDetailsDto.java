package com.tripjoy.api.dto.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GooglePlaceDetailsDto {
    private String id;
    private DisplayName displayName;
    private String formattedAddress;
    private Location location;
    private List<String> types;
    private String primaryType;
    private Double rating;
    private Integer userRatingCount;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DisplayName {
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private Double latitude;
        private Double longitude;
    }
}
