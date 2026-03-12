package com.tripjoy.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.GroupRequest;
import com.tripjoy.api.dto.request.member.AddMemberRequest;
import com.tripjoy.api.dto.request.member.TransferLeadershipRequest;
import com.tripjoy.api.dto.request.member.UpdateMemberRoleRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.GroupMemberResponse;
import com.tripjoy.api.dto.response.GroupResponse;
import com.tripjoy.api.service.IGroupService;
import com.tripjoy.api.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.Group.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Groups", description = "Manage travel groups (Core business)")
public class GroupController {

    IGroupService groupService;

    // --- GROUP CRUD ---

    @Operation(summary = "Create a new group (Automatically creates General chat channel) - OK")
    @PostMapping
    public ApiResponse<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request) {
        UUID ownerId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<GroupResponse>builder()
                .data(groupService.createGroup(request, ownerId))
                .build();
    }

    @Operation(summary = "Get my groups list - OK")
    @GetMapping
    public ApiResponse<List<GroupResponse>> getMyGroups() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<List<GroupResponse>>builder()
                .data(groupService.getMyGroups(currentUserId))
                .build();
    }

    @Operation(summary = "Get group information by ID - OK")
    @GetMapping(Endpoint.Group.ID)
    public ApiResponse<GroupResponse> getGroupById(@PathVariable UUID groupId) {
        return ApiResponse.<GroupResponse>builder()
                .data(groupService.getGroupById(groupId))
                .build();
    }

    @Operation(summary = "Update group information - OK")
    @PutMapping(Endpoint.Group.ID)
    public ApiResponse<GroupResponse> updateGroup(
            @PathVariable UUID groupId, @Valid @RequestBody GroupRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<GroupResponse>builder()
                .data(groupService.updateGroup(groupId, request, currentUserId))
                .build();
    }

    @Operation(summary = "Delete group (soft delete with cascade) - Only LEADER - OK")
    @DeleteMapping(Endpoint.Group.ID)
    public ApiResponse<Void> deleteGroup(@PathVariable UUID groupId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        groupService.deleteGroup(groupId, currentUserId);
        return ApiResponse.<Void>builder().message("Group deleted successfully").build();
    }

    // --- MEMBERS MANAGEMENT (Source of Truth) ---

    @Operation(summary = "Add member to group (Automatically syncs to Chat) - OK")
    @PostMapping(Endpoint.Group.MEMBERS_BASE)
    public ApiResponse<Void> addMember(@PathVariable UUID groupId, @Valid @RequestBody AddMemberRequest request) {

        // Service bắn Event MemberJoined -> Tự thêm vào Chat
        // Giả sử request.getUserId() trả về UUID của người được thêm
        groupService.addMemberToGroup(groupId, request.getMemberId());

        return ApiResponse.<Void>builder()
                .message("Member added successfully and syncing to chat...")
                .build();
    }

    @Operation(summary = "Get group members list - OK")
    @GetMapping(Endpoint.Group.MEMBERS_BASE)
    public ApiResponse<List<GroupMemberResponse>> getMembers(@PathVariable UUID groupId) {
        return ApiResponse.<List<GroupMemberResponse>>builder()
                .data(groupService.getGroupMembers(groupId))
                .build();
    }

    @Operation(summary = "Remove member from group (Automatically kicks from Chat) - OK")
    @DeleteMapping(Endpoint.Group.MEMBERS_ID)
    public ApiResponse<Void> removeMember(@PathVariable UUID groupId, @PathVariable UUID memberId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        groupService.removeMemberFromGroup(groupId, memberId, currentUserId);
        return ApiResponse.<Void>builder()
                .message("Member removed successfully and kicked from chat")
                .build();
    }

    @Operation(summary = "Leave group (Self-initiated) - OK")
    @DeleteMapping(Endpoint.Group.MEMBERS_ME)
    public ApiResponse<Void> leaveGroup(@PathVariable UUID groupId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        groupService.leaveGroup(groupId, currentUserId);
        return ApiResponse.<Void>builder()
                .message("You have left the group successfully")
                .build();
    }

    @Operation(summary = "Update member role (Leader/CO_LEADER/Member) - OK")
    @PutMapping(Endpoint.Group.MEMBERS_ID)
    public ApiResponse<GroupMemberResponse> updateMemberRole(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ApiResponse.<GroupMemberResponse>builder()
                .data(groupService.updateMemberRole(groupId, memberId, request, currentUserId))
                .build();
    }

    @Operation(summary = "Transfer leadership to another member - OK")
    @PostMapping(Endpoint.Group.ID + "/transfer-leadership")
    public ApiResponse<Void> transferLeadership(
            @PathVariable UUID groupId, @Valid @RequestBody TransferLeadershipRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        groupService.transferLeadership(groupId, request, currentUserId);
        return ApiResponse.<Void>builder()
                .message("Leadership transferred successfully")
                .build();
    }
}
