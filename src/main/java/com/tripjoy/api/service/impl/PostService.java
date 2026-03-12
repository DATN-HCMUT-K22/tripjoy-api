package com.tripjoy.api.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.dto.request.PostSearchRequest;
import com.tripjoy.api.dto.response.PostResponse;
import com.tripjoy.api.entity.Post;
import com.tripjoy.api.mapper.PostMapper;
import com.tripjoy.api.repository.PostRepository;
import com.tripjoy.api.service.IPostService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService implements IPostService {

    PostRepository postRepository;
    PostMapper postMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(PostSearchRequest request, UUID currentUserId) {
        // Enforce pagination limits
        int page = Math.max(0, request.getPage());
        int size = Math.min(50, Math.max(1, request.getSize())); // Max 50 items per page
        int offset = page * size;

        // Clean text inputs
        String keyword = request.getQ() != null && !request.getQ().trim().isEmpty() ? request.getQ().trim() : null;
        String hashtag = request.getHashtag() != null && !request.getHashtag().trim().isEmpty() ? request.getHashtag().trim() : null;
        
        // Remove # from hashtag if user included it
        if (hashtag != null && hashtag.startsWith("#")) {
            hashtag = hashtag.substring(1);
        }

        // 1. Query for posts data
        List<Post> posts = postRepository.searchPosts(
                keyword,
                hashtag,
                request.getCreatorId(),
                request.getItineraryId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMinDays(),
                request.getMaxDays(),
                request.getMinBudget(),
                request.getMaxBudget(),
                request.getMinPeople(),
                request.getMaxPeople(),
                request.getOriginId(),
                request.getDestinationId(),
                request.getSort(),
                size,
                offset);

        // 2. Count total results for pagination metadata
        long totalElements = 0;
        if (!posts.isEmpty() || page > 0) {
            totalElements = postRepository.countSearchPosts(
                    keyword,
                    hashtag,
                    request.getCreatorId(),
                    request.getItineraryId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getMinDays(),
                    request.getMaxDays(),
                    request.getMinBudget(),
                    request.getMaxBudget(),
                    request.getMinPeople(),
                    request.getMaxPeople(),
                    request.getOriginId(),
                    request.getDestinationId());
        }

        // 3. Map entities to Responses, handling contextual fields manually
        List<PostResponse> responses = posts.stream().map(post -> {
            PostResponse response = postMapper.toPostResponse(post);
            
            // Context logic: check if the current user liked or saved the post
            if (currentUserId != null) {
                response.setIsLiked(post.getLikeUsers() != null 
                    && post.getLikeUsers().stream().anyMatch(user -> user.getId().equals(currentUserId)));
                response.setIsSaved(post.getSaveUsers() != null 
                    && post.getSaveUsers().stream().anyMatch(user -> user.getId().equals(currentUserId)));
            } else {
                response.setIsLiked(false);
                response.setIsSaved(false);
            }
            
            return response;
        }).toList();

        return new PageImpl<>(responses, PageRequest.of(page, size), totalElements);
    }
}
