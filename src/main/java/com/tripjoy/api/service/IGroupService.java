package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.GroupRequest;
import com.tripjoy.api.dto.response.GroupResponse;
import com.tripjoy.api.dto.response.simple.GroupMemberResponse;

import java.util.List;
import java.util.UUID;

public interface IGroupService {
    GroupResponse createGroup(GroupRequest request, UUID ownerId);

    GroupMemberResponse addMemberToGroup(UUID groupId, UUID userId);

    GroupResponse getGroupById(UUID groupId);

    List<GroupResponse> getMyGroups(UUID userId);
}
