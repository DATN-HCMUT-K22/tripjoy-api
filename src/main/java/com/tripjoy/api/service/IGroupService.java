package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.GroupRequest;
import com.tripjoy.api.dto.response.GroupResponse;
import com.tripjoy.api.dto.response.GroupMemberResponse;

import java.util.List;
import java.util.UUID;

public interface IGroupService {
    GroupResponse createGroup(GroupRequest request, UUID ownerId);

    GroupMemberResponse addMemberToGroup(UUID groupId, UUID userId);

    GroupResponse getGroupById(UUID groupId);

    List<GroupResponse> getMyGroups(UUID userId);

    GroupResponse updateGroup(UUID groupId, GroupRequest request, UUID currentUserId);

    List<GroupMemberResponse> getGroupMembers(UUID groupId);

    void removeMemberFromGroup(UUID groupId, UUID memberId, UUID currentUserId);

    void leaveGroup(UUID groupId, UUID currentUserId);
}
