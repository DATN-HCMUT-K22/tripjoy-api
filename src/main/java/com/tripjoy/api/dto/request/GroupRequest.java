package com.tripjoy.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupRequest {

    @NotBlank
    @Schema(
            name = "name",
            description = "Name of the travel group",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Da Nang Avengers"
    )
    String name;

    @Schema(
            name = "avatar",
            description = "URL of the group's avatar image",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "https://res.cloudinary.com/tripjoy/image/upload/group_avatar.jpg"
    )
    String avatar;

    @Schema(
            name = "theme",
            description = "Theme of the group",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Adventure"
    )
    String theme;
}