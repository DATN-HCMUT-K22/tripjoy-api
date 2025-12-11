package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    String theme;

    @JsonProperty("theme_color")
    String themeColor;

    @JsonProperty("is_pro")
    Boolean isPro;

    @JsonProperty("chatbot_count")
    Integer chatbotCount;

    // Lồng danh sách thành viên
    Set<GroupMemberResponse> members;
}