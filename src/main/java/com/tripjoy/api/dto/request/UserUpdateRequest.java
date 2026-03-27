package com.tripjoy.api.dto.request;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "UserUpdateRequest", description = "Request payload for updating a users's profile")
public class UserUpdateRequest {

    @Schema(
            name = "password",
            description = "New password for the users (leave empty to keep current password)",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "NewStr0ngP@ss")
    String password;

    @Schema(
            name = "fullName",
            description = "Users's full name",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "John Doe")
    String fullName;

    @Schema(
            name = "bio",
            description = "Short biography or profile description",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Traveler. Photographer. Coffee lover.")
    String bio;

    @Schema(
            name = "avatarUrl",
            description = "URL to the users's avatar image",
            type = "String",
            format = "uri",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "https://example.com/avatars/johndoe.jpg")
    String avatarUrl;

    @Schema(
            name = "isDeleted",
            description = "Flag indicating whether the users is marked as deleted",
            type = "boolean",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "false")
    boolean isDeleted = false;

    @Schema(
            name = "isLocked",
            description = "Flag indicating whether the users account is locked",
            type = "boolean",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "false")
    boolean isLocked = false;

    @Schema(
            name = "dateOfBirth",
            description = "Users's date of birth",
            type = "string",
            format = "date",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "1990-01-01")
    LocalDate dateOfBirth;

    @Schema(
            name = "roles",
            description = "List of role names assigned to the users",
            type = "array",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "[\"USER\", \"ADMIN\"]")
    List<String> roles;
}
