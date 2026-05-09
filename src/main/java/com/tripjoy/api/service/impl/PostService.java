package com.tripjoy.api.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.dto.event.PostLikedEvent;
import com.tripjoy.api.dto.request.PostQueryParams;
import com.tripjoy.api.dto.request.PostRequest;
import com.tripjoy.api.dto.response.PostResponse;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.Post;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;
import com.tripjoy.api.enums.PostVisibility;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.PostMapper;
import com.tripjoy.api.repository.ItineraryRepository;
import com.tripjoy.api.repository.PostRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IHashtagService;
import com.tripjoy.api.service.IPostService;
import com.tripjoy.api.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService implements IPostService {

    PostRepository postRepository;
    PostMapper postMapper;
    UserRepository userRepository;
    ItineraryRepository itineraryRepository;
    IHashtagService hashtagService;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PostResponse createPost(PostRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.builder()
                .content(request.getContent())
                .mediaUrls(request.getMediaUrls())
                .creator(user)
                .shareQuantity(0)
                .visibility(request.getVisibility() != null ? request.getVisibility() : PostVisibility.PUBLIC)
                .softDeleteInfo(new SoftDeleteInfo())
                .build();

        if (request.getItineraryId() != null) {
            Itinerary itinerary = itineraryRepository
                    .findById(request.getItineraryId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            post.setItinerary(itinerary);
        }

        if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {
            post.setHashtags(hashtagService.syncHashtags(request.getHashtags()));
        }

        post = postRepository.save(post);
        return getPostResponseWithContext(post, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(PostQueryParams params, Pageable pageable, UUID currentUserId) {
        // Fast path: no filter criteria — use simple paginated findAll (no FTS overhead)
        if (params == null || params.isEmpty()) {
            Page<Post> postPage = postRepository.findBySoftDeleteInfoIsDeletedFalse(pageable);
            List<PostResponse> responses = getPostResponsesWithContext(postPage.getContent(), currentUserId);
            return new PageImpl<>(responses, pageable, postPage.getTotalElements());
        }

        // Filter path: delegate to FTS + multi-criteria native query
        // The native query manages its own offset/limit for now (uses PostQueryParams.sort internally)
        int pageNum = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int offset = pageNum * size;

        String keyword = params.normalizedKeyword();
        String hashtag = params.normalizedHashtag();

        List<Post> posts = postRepository.searchPosts(
                keyword,
                hashtag,
                params.getCreatorId(),
                params.getItineraryId(),
                params.getStartDate(),
                params.getEndDate(),
                params.getMinDays(),
                params.getMaxDays(),
                params.getMinBudget(),
                params.getMaxBudget(),
                params.getMinPeople(),
                params.getMaxPeople(),
                params.getOriginId(),
                params.getDestinationId(),
                params.getSort(),
                size,
                offset);

        long totalElements = 0;
        if (!posts.isEmpty() || pageNum > 0) {
            totalElements = postRepository.countSearchPosts(
                    keyword,
                    hashtag,
                    params.getCreatorId(),
                    params.getItineraryId(),
                    params.getStartDate(),
                    params.getEndDate(),
                    params.getMinDays(),
                    params.getMaxDays(),
                    params.getMinBudget(),
                    params.getMaxBudget(),
                    params.getMinPeople(),
                    params.getMaxPeople(),
                    params.getOriginId(),
                    params.getDestinationId());
        }

        List<PostResponse> responses = getPostResponsesWithContext(posts, currentUserId);

        return new PageImpl<>(responses, pageable, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(UUID postId, UUID currentUserId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (post.getSoftDeleteInfo() != null && post.getSoftDeleteInfo().isDeleted()) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        return getPostResponseWithContext(post, currentUserId);
    }

    @Override
    @Transactional
    public PostResponse updatePost(UUID postId, PostRequest request) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(post);

        post.setContent(request.getContent());
        post.setMediaUrls(request.getMediaUrls());

        if (request.getItineraryId() != null) {
            Itinerary itinerary = itineraryRepository
                    .findById(request.getItineraryId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            post.setItinerary(itinerary);
        } else {
            post.setItinerary(null);
        }

        if (request.getHashtags() != null) {
            post.setHashtags(hashtagService.syncHashtags(request.getHashtags()));
        }

        post = postRepository.save(post);
        return getPostResponseWithContext(post, SecurityUtils.getCurrentUserId());
    }

    @Override
    @Transactional
    public void deletePost(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(post);

        post.getSoftDeleteInfo().markAsDeleted(SecurityUtils.getCurrentUserId().toString());
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void likePost(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User user = userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        post.getLikeUsers().add(user);
        postRepository.save(post);

        eventPublisher.publishEvent(new PostLikedEvent(post, user));
    }

    @Override
    @Transactional
    public void unlikePost(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User user = userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        post.getLikeUsers().remove(user);
        postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getSavedPosts(Pageable pageable, UUID currentUserId) {
        Page<Post> postPage = postRepository.findBySaveUsersIdAndSoftDeleteInfoIsDeletedFalse(currentUserId, pageable);
        List<PostResponse> responses = getPostResponsesWithContext(postPage.getContent(), currentUserId);
        return new PageImpl<>(responses, pageable, postPage.getTotalElements());
    }

    @Override
    @Transactional
    public void savePost(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User user = userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        post.getSaveUsers().add(user);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void unsavePost(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User user = userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        post.getSaveUsers().remove(user);
        postRepository.save(post);
    }

    private PostResponse getPostResponseWithContext(Post post, UUID currentUserId) {
        PostResponse response = postMapper.toPostResponse(post);
        if (currentUserId != null) {
            response.setIsLiked(postRepository.isLikedByUser(post.getId(), currentUserId));
            response.setIsSaved(postRepository.isSavedByUser(post.getId(), currentUserId));
        } else {
            response.setIsLiked(false);
            response.setIsSaved(false);
        }
        return response;
    }

    private List<PostResponse> getPostResponsesWithContext(List<Post> posts, UUID currentUserId) {
        if (posts.isEmpty()) return List.of();

        List<PostResponse> responses =
                posts.stream().map(postMapper::toPostResponse).collect(Collectors.toList());

        if (currentUserId != null) {
            List<UUID> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
            List<UUID> likedIds = postRepository.findLikedPostIdsByUser(postIds, currentUserId);
            List<UUID> savedIds = postRepository.findSavedPostIdsByUser(postIds, currentUserId);

            responses.forEach(res -> {
                res.setIsLiked(likedIds.contains(res.getId()));
                res.setIsSaved(savedIds.contains(res.getId()));
            });
        } else {
            responses.forEach(res -> {
                res.setIsLiked(false);
                res.setIsSaved(false);
            });
        }
        return responses;
    }

    private void validateOwnership(Post post) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (post.getCreator() == null || !post.getCreator().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
}
