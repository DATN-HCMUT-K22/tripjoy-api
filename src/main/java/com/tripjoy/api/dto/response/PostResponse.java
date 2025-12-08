package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.ItinerarySimpleResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {

    UUID id;

    @JsonProperty("media_url")
    String mediaUrl;

    String content;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("updated_at")
    LocalDateTime updatedAt;

    @JsonProperty("shared_quantity")
    Integer sharedQuantity;

    // Lấy từ bảng Create_post
    @JsonProperty("itinerary")
    ItinerarySimpleResponse itinerary; // (nullable)

    // Lấy từ bảng Post_hashtag
    Set<String> hashtags;

    // Lồng thông tin người tạo
    @JsonProperty("created_by")
    UserSimpleResponse createdBy;

    // --- Dữ liệu theo ngữ cảnh ---
    // (Lấy từ bảng Like_post, Save_post, Comment)
    @JsonProperty("like_count")
    Long likeCount;

    @JsonProperty("comment_count")
    Long commentCount;

    @JsonProperty("is_liked")
    Boolean isLiked; // Users này đã like post chưa?

    @JsonProperty("is_saved")
    Boolean isSaved; // Users này đã save post chưa?

    @JsonProperty("latest_comments")
    List<CommentResponse> latestComments;
}