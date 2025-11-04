package com.tripjoy.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "UserUpdateRequest", description = "Request payload for updating a user's profile")
public class UserUpdateRequest {

    @Schema(
            name = "password",
            description = "New password for the user (leave empty to keep current password)",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "NewStr0ngP@ss"
    )
    String password;

    @Schema(
            name = "fullName",
            description = "User's full name",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "John Doe"
    )
    String fullName;

    @Schema(
            name = "bio",
            description = "Short biography or profile description",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Traveler. Photographer. Coffee lover."
    )
    String bio;

    @Schema(
            name = "avatarUrl",
            description = "URL to the user's avatar image",
            type = "String",
            format = "uri",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "https://example.com/avatars/johndoe.jpg"
    )
    String avatarUrl;

    @Schema(
            name = "isDeleted",
            description = "Flag indicating whether the user is marked as deleted",
            type = "boolean",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "false"
    )
    boolean isDeleted = false;

    @Schema(
            name = "isLocked",
            description = "Flag indicating whether the user account is locked",
            type = "boolean",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "false"
    )
    boolean isLocked = false;

    @Schema(
            name = "dateOfBirth",
            description = "User's date of birth",
            type = "string",
            format = "date",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "1990-01-01"
    )
    LocalDate dateOfBirth;

    @Schema(
            name = "roles",
            description = "List of role names assigned to the user",
            type = "array",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "[\"USER\", \"ADMIN\"]"
    )
    List<String> roles;
}
