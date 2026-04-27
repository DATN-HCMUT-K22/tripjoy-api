package com.tripjoy.api.dto.response;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupResponse extends BaseResponse {

    UUID id;
    String name;
    String description;
    String avatar;
    @JsonProperty("theme_color")
    String themeColor;

    @JsonProperty("is_pro")
    Boolean isPro;

    @JsonProperty("chatbot_count")
    Integer chatbotCount;

    @JsonProperty("iti_count")
    Integer itiCount;

    Boolean isDeleted;

    // Lồng danh sách thành viên
    Set<GroupMemberResponse> members;
}
