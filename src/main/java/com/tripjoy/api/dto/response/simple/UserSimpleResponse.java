package com.tripjoy.api.dto.response.simple;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSimpleResponse {
    String id;

    String username;

    String fullName;

    String avatarUrl;
}
