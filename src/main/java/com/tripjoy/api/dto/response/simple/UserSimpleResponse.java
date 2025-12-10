package com.tripjoy.api.dto.response.simple;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSimpleResponse {
    UUID id;

    String username;

    String fullName;

    String avatarUrl;
}
