package com.tripjoy.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.tripjoy.api.dto.request.PostSearchRequest;
import com.tripjoy.api.dto.response.PostResponse;

public interface IPostService {
    Page<PostResponse> searchPosts(PostSearchRequest request, UUID currentUserId);
}
