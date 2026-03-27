package com.tripjoy.api.dto.response.simple;

import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupSimpleResponse {
    UUID id;
    String name;
    String avatarUrl;
    Integer chatbotCount;
    Boolean isPro;
}
