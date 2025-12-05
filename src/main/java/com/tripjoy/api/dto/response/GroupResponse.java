package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.GroupMemberResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupResponse {

    String id;
    String name;
    String avatar;
    String theme;

    @JsonProperty("theme_color")
    String themeColor;

    @JsonProperty("is_pro")
    Boolean isPro;

    @JsonProperty("chatbot_count") // Map đúng tên cột DB cho dễ hiểu
    Integer chatbotCount;

    // Lồng danh sách thành viên
    List<GroupMemberResponse> members;
}