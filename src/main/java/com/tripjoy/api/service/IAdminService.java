package com.tripjoy.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.request.report.ModerationActionRequest;
import com.tripjoy.api.dto.response.report.ModerationActionResponse;

public interface IAdminService {
    ModerationActionResponse moderateUser(ModerationActionRequest request);

    Page<ModerationActionResponse> getModerationActions(
            UUID userId, String actionType, UUID baId, Pageable pageable);
}

