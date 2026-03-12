package com.tripjoy.api.dto.response;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse extends BaseResponse {

    @Schema(description = "UUID của comment")
    UUID id;

    @Schema(description = "Nội dung comment")
    String content;

    // createdAt, updatedAt đã có từ BaseResponse - REMOVED

    // Lồng thông tin người tạo
    @JsonProperty("created_by_user") // Renamed để tránh conflict
    UserSimpleResponse createdByUser;

    // --- Dữ liệu theo ngữ cảnh ---
    @JsonProperty("like_count")
    @Schema(description = "Tổng số lượt thích")
    Long likeCount;

    @JsonProperty("is_liked")
    @Schema(description = "Users hiện tại đã thích comment này chưa")
    Boolean isLiked;

    // --- Xử lý Nested ---

    @JsonProperty("post_id")
    @Schema(description = "ID của bài post chứa comment này")
    String postId; // Hữu ích cho client

    @JsonProperty("parent_comment_id")
    @Schema(description = "ID của comment cha (null nếu là comment gốc)")
    String parentCommentId;

    @JsonProperty("reply_count")
    @Schema(description = "Tổng số reply của comment này")
    Integer replyCount;

    @JsonProperty("latest_replies")
    @Schema(description = "Danh sách một vài reply mới nhất (ví dụ: 2 reply)")
    List<CommentResponse> latestReplies; // Dùng chính DTO này, nhưng sẽ được xử lý ở service
}
