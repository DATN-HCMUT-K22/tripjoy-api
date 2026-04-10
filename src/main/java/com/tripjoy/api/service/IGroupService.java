package com.tripjoy.api.service;

import java.util.List;
import java.util.UUID;
import com.tripjoy.api.enums.GroupRole;

import com.tripjoy.api.dto.request.GroupRequest;
import com.tripjoy.api.dto.request.member.TransferLeadershipRequest;
import com.tripjoy.api.dto.request.member.UpdateMemberRoleRequest;
import com.tripjoy.api.dto.response.GroupMemberResponse;
import com.tripjoy.api.dto.response.GroupResponse;

public interface IGroupService {
    GroupResponse createGroup(GroupRequest request, UUID ownerId);

    GroupMemberResponse addMemberToGroup(UUID groupId, UUID userId, GroupRole role);

    GroupResponse getGroupById(UUID groupId);

    List<GroupResponse> getMyGroups(UUID userId);

    GroupResponse updateGroup(UUID groupId, GroupRequest request, UUID currentUserId);

    List<GroupMemberResponse> getGroupMembers(UUID groupId);

    void removeMemberFromGroup(UUID groupId, UUID memberId, UUID currentUserId);

    void leaveGroup(UUID groupId, UUID currentUserId);

    GroupMemberResponse updateMemberRole(
            UUID groupId, UUID memberId, UpdateMemberRoleRequest request, UUID currentUserId);

    void transferLeadership(UUID groupId, TransferLeadershipRequest request, UUID currentUserId);

    // === SOFT DELETE CASCADE METHODS ===

    void deleteGroup(UUID groupId, UUID currentUserId);

    void restoreGroup(UUID groupId, UUID currentUserId);

    List<GroupResponse> searchGroups(String keyword);
}
