package com.tripjoy.api.dto.context;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostMappingContext {
    private final Set<UUID> likedPostIds;
    private final Set<UUID> savedPostIds;

    public boolean isLiked(UUID postId) {
        return likedPostIds != null && likedPostIds.contains(postId);
    }

    public boolean isSaved(UUID postId) {
        return savedPostIds != null && savedPostIds.contains(postId);
    }

    public static PostMappingContext empty() {
        return new PostMappingContext(Collections.emptySet(), Collections.emptySet());
    }
}
