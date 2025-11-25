package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {

    @Schema(description = "UUID của comment")
    String id;

    @Schema(description = "Nội dung comment")
    String content;

    @JsonProperty("created_at")
    @Schema(description = "Thời điểm tạo")
    LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @Schema(description = "Thời điểm cập nhật")
    LocalDateTime updatedAt;

    // Lồng thông tin người tạo
    @JsonProperty("created_by")
    UserSimpleResponse createdBy;

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