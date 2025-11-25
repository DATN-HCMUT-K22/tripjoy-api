package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.SuggestLocationRequest;
import com.tripjoy.api.dto.request.member.AddMemberRequest;
import com.tripjoy.api.dto.request.GroupRequest;
import com.tripjoy.api.dto.request.member.UpdateMemberRoleRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.GroupResponse;
import com.tripjoy.api.dto.response.SuggestLocationResponse;
import com.tripjoy.api.dto.response.simple.GroupMemberResponse;
import com.tripjoy.api.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Endpoint.Group.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Groups", description = "Endpoints for managing groups and members")
public class GroupController {

    GroupService groupService;

    @Operation(summary = "Create a new group")
    @PostMapping
    public ApiResponse<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request) {
        return ApiResponse.<GroupResponse>builder()
//                .data(groupService.createGroup(request))
                .build();
    }

    @Operation(summary = "Get group details by ID")
    @GetMapping(Endpoint.Group.ID)
    public ApiResponse<GroupResponse> getGroupById(@PathVariable String groupId) {
        return ApiResponse.<GroupResponse>builder()
//                .data(groupService.getGroupById(groupId))
                .build();
    }

    @Operation(summary = "Update group details")
    @PutMapping(Endpoint.Group.ID)
    public ApiResponse<GroupResponse> updateGroup(@PathVariable String groupId, @Valid @RequestBody GroupRequest request) {
        return ApiResponse.<GroupResponse>builder()
//                .data(groupService.updateGroup(groupId, request))
                .build();
    }

    @Operation(summary = "Delete a group")
    @DeleteMapping(Endpoint.Group.ID)
    public ApiResponse<Void> deleteGroup(@PathVariable String groupId) {
//        groupService.deleteGroup(groupId);
        return ApiResponse.<Void>builder().message("Groups deleted successfully").build();
    }

    // --- Nested Groups Members ---

    @Operation(summary = "Add a member to a group")
    @PostMapping(Endpoint.Group.MEMBERS_BASE)
    public ApiResponse<GroupMemberResponse> addMember(@PathVariable String groupId, @Valid @RequestBody AddMemberRequest request) {
        return ApiResponse.<GroupMemberResponse>builder()
//                .data(groupService.addMember(groupId, request))
                .build();
    }

    @Operation(summary = "Get all members of a group")
    @GetMapping(Endpoint.Group.MEMBERS_BASE)
    public ApiResponse<List<GroupMemberResponse>> getMembers(@PathVariable String groupId) {
        return ApiResponse.<List<GroupMemberResponse>>builder()
//                .data(groupService.getMembers(groupId))
                .build();
    }

    @Operation(summary = "Remove a member from a group")
    @DeleteMapping(Endpoint.Group.MEMBERS_ID)
    public ApiResponse<Void> removeMember(@PathVariable String groupId, @PathVariable String memberId) {
//        groupService.removeMember(groupId, memberId);
        return ApiResponse.<Void>builder().message("Member removed successfully").build();
    }

    @Operation(summary = "Update a member's role (e.g., set as leader)")
    @PutMapping(Endpoint.Group.MEMBERS_ID)
    public ApiResponse<GroupMemberResponse> updateMemberRole(@PathVariable String groupId, @PathVariable String memberId, @Valid @RequestBody UpdateMemberRoleRequest request) {
        return ApiResponse.<GroupMemberResponse>builder()
//                .data(groupService.updateMemberRole(groupId, memberId, request))
                .build();
    }
    @Operation(summary = "Suggest a new location for the group")
    @PostMapping(Endpoint.Group.SUGGEST_LOCATIONS)
    public ApiResponse<SuggestLocationResponse> suggestLocation(
            @PathVariable String groupId,
            @Valid @RequestBody SuggestLocationRequest request) {

         return ApiResponse.<SuggestLocationResponse>builder()
        //        .data(groupService.suggestLocation(groupId, request))
                .build();
    }

    @Operation(summary = "Get all suggested locations for the group")
    @GetMapping(Endpoint.Group.SUGGEST_LOCATIONS)
    public ApiResponse<List<SuggestLocationResponse>> getSuggestedLocations(
            @PathVariable String groupId) {

         return ApiResponse.<List<SuggestLocationResponse>>builder()
//                .data(groupService.getSuggestedLocations(groupId))
                .build();
    }
}