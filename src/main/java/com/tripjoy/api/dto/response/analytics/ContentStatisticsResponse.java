package com.tripjoy.api.dto.response.analytics;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContentStatisticsResponse {

    @JsonProperty("total_posts")
    Long totalPosts;

    @JsonProperty("total_comments")
    Long totalComments;

    @JsonProperty("posts_created_today")
    Long postsCreatedToday;

    @JsonProperty("comments_created_today")
    Long commentsCreatedToday;

    @JsonProperty("deleted_posts")
    Long deletedPosts;

    @JsonProperty("deleted_comments")
    Long deletedComments;

    @JsonProperty("avg_posts_per_user")
    Double avgPostsPerUser;

    @JsonProperty("most_active_users")
    List<MostActiveUserDto> mostActiveUsers;
}
