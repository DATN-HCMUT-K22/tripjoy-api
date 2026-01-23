package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCursorResponse {

    private List<ChatMessageResponse> messages;

    private CursorInfo cursors;

    @JsonProperty("has_more")
    private HasMore hasMore;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CursorInfo {
        private String before; // ISO timestamp for loading older messages
        private String after; // ISO timestamp for loading newer messages
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HasMore {
        private Boolean before; // Has older messages?
        private Boolean after; // Has newer messages?
    }
}
