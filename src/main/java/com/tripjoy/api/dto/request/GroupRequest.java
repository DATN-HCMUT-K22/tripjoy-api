package com.tripjoy.api.dto.request;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @NotBlank(message = "INVALID_REQUEST")
    @Schema(
            name = "name",
            description = "Name of the travel group",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Da Nang Avengers")
    String name;

    @Schema(
            name = "avatar",
            description = "URL of the group's avatar image",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "https://res.cloudinary.com/tripjoy/image/upload/group_avatar.jpg")
    String avatar;

    @Schema(
            name = "description",
            description = "Description of the travel group",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "")
    String description;

    private Integer chatbotCount;
    private Boolean isPro;

    @JsonProperty("theme_color")
    @Schema(name = "theme_color", description = "Hex color code for group theme", example = "#FF5733")
    String themeColor;

    // Optional: Add members ngay khi tạo
    @JsonProperty("member_ids")
    Set<UUID> memberIds;
}
