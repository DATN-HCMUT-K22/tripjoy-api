package com.tripjoy.api.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "UserProfileUpdateRequest", description = "Request payload for updating a user's profile")
public class UserProfileUpdateRequest {

    @Schema(name = "fullName", description = "User's full name", type = "String", example = "John Doe")
    String fullName;

    @Schema(name = "phoneNumber", description = "User's phone number", type = "String", example = "0123456789")
    String phoneNumber;

    @Schema(
            name = "bio",
            description = "Short biography or profile description",
            type = "String",
            example = "Traveler. Photographer. Coffee lover.")
    String bio;

    @Schema(
            name = "avatarUrl",
            description = "URL to the user's avatar image",
            type = "String",
            format = "uri",
            example = "https://example.com/avatars/johndoe.jpg")
    String avatarUrl;

    @Schema(
            name = "dateOfBirth",
            description = "User's date of birth",
            type = "string",
            format = "date",
            example = "1990-01-01")
    LocalDate dateOfBirth;
}
