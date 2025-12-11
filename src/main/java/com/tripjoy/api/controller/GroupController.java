package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.SuggestLocationRequest;
import com.tripjoy.api.dto.request.member.AddMemberRequest;
import com.tripjoy.api.dto.request.GroupRequest;
import com.tripjoy.api.dto.request.member.UpdateMemberRoleRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.GroupResponse;
import com.tripjoy.api.dto.response.SuggestLocationResponse;
import com.tripjoy.api.dto.response.GroupMemberResponse;
import com.tripjoy.api.service.IGroupService;
import com.tripjoy.api.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

        @Operation(summary = "Get group information by ID")
        @GetMapping(Endpoint.Group.ID)
        public ApiResponse<GroupResponse> getGroupById(@PathVariable UUID groupId) {
                return ApiResponse.<GroupResponse>builder()
                                .data(groupService.getGroupById(groupId))
                                .build();
        }

        @Operation(summary = "Update group information - OK")
        @PutMapping(Endpoint.Group.ID)
        public ApiResponse<GroupResponse> updateGroup(
                        @PathVariable UUID groupId,
                        @Valid @RequestBody GroupRequest request) {
                UUID currentUserId = SecurityUtils.getCurrentUserId();

                return ApiResponse.<GroupResponse>builder()
                                .data(groupService.updateGroup(groupId, request, currentUserId))
                                .build();
        }

        @Operation(summary = "Delete group")
        @DeleteMapping(Endpoint.Group.ID)
        public ApiResponse<Void> deleteGroup(@PathVariable UUID groupId) {
                // groupService.deleteGroup(groupId);
                return ApiResponse.<Void>builder().message("Group deleted successfully").build();
        }

        // --- MEMBERS MANAGEMENT (Source of Truth) ---

        @Operation(summary = "Add member to group (Automatically syncs to Chat) - OK")
        @PostMapping(Endpoint.Group.MEMBERS_BASE)
        public ApiResponse<Void> addMember(
                        @PathVariable UUID groupId,
                        @Valid @RequestBody AddMemberRequest request) {

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

        @Operation(summary = "Remove member from group (Automatically kicks from Chat)")
        @DeleteMapping(Endpoint.Group.MEMBERS_ID)
        public ApiResponse<Void> removeMember(
                        @PathVariable UUID groupId,
                        @PathVariable UUID memberId) {
                // groupService.removeMember(groupId, memberId);
                return ApiResponse.<Void>builder().message("Member removed successfully").build();
        }

        @Operation(summary = "Update member role (Leader/Member)")
        @PutMapping(Endpoint.Group.MEMBERS_ID)
        public ApiResponse<GroupMemberResponse> updateMemberRole(
                        @PathVariable UUID groupId,
                        @PathVariable UUID memberId,
                        @Valid @RequestBody UpdateMemberRoleRequest request) {
                return ApiResponse.<GroupMemberResponse>builder()
                                // .data(groupService.updateMemberRole(groupId, memberId, request))
                                .build();
        }

        // --- LOCATION SUGGESTIONS ---

        @Operation(summary = "Suggest location for group")
        @PostMapping(Endpoint.Group.LOCATION_SUGGESTIONS)
        public ApiResponse<SuggestLocationResponse> suggestLocation(
                        @PathVariable UUID groupId,
                        @Valid @RequestBody SuggestLocationRequest request) {
                return ApiResponse.<SuggestLocationResponse>builder()
                                // .data(groupService.suggestLocation(groupId, request))
                                .build();
        }

        @Operation(summary = "Get list of suggested locations")
        @GetMapping(Endpoint.Group.LOCATION_SUGGESTIONS)
        public ApiResponse<List<SuggestLocationResponse>> getSuggestedLocations(@PathVariable UUID groupId) {
                return ApiResponse.<List<SuggestLocationResponse>>builder()
                                // .data(groupService.getSuggestedLocations(groupId))
                                .build();
        }
}