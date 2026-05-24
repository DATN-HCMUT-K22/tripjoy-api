package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.report.ModerationActionRequest;
import com.tripjoy.api.dto.response.report.ModerationActionResponse;

public interface IAdminService {
    ModerationActionResponse moderateUser(ModerationActionRequest request);
}
