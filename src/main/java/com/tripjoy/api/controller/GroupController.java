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
import com.tripjoy.api.entity.User;
import com.tripjoy.api.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Endpoint.Group.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Groups", description = "Quản lý nhóm đi chơi (Nghiệp vụ cốt lõi)")
public class GroupController {

    GroupService groupService;

    // --- GROUP CRUD ---

    @Operation(summary = "Tạo nhóm mới (Tự động tạo kênh chat General)")
    @PostMapping
    public ApiResponse<GroupResponse> createGroup(
            @Valid @RequestBody GroupRequest request,
            @AuthenticationPrincipal User currentUser // Lấy user từ token
    ) {
        // Service sẽ bắn Event GroupCreated -> Tự tạo Chat
        return ApiResponse.<GroupResponse>builder()
                .data(groupService.createGroup(request, currentUser))
                .build();
    }

    @Operation(summary = "Lấy thông tin nhóm theo ID")
    @GetMapping(Endpoint.Group.ID)
    public ApiResponse<GroupResponse> getGroupById(@PathVariable UUID groupId) {
        return ApiResponse.<GroupResponse>builder()
//                .data(groupService.getGroupById(groupId))
                .build();
    }

    @Operation(summary = "Cập nhật thông tin nhóm")
    @PutMapping(Endpoint.Group.ID)
    public ApiResponse<GroupResponse> updateGroup(
            @PathVariable UUID groupId,
            @Valid @RequestBody GroupRequest request) {
        return ApiResponse.<GroupResponse>builder()
//                .data(groupService.updateGroup(groupId, request))
                .build();
    }

    @Operation(summary = "Xóa nhóm")
    @DeleteMapping(Endpoint.Group.ID)
    public ApiResponse<Void> deleteGroup(@PathVariable UUID groupId) {
//        groupService.deleteGroup(groupId);
        return ApiResponse.<Void>builder().message("Group deleted successfully").build();
    }

    // --- MEMBERS MANAGEMENT (Source of Truth) ---

    @Operation(summary = "Thêm thành viên vào nhóm (Tự động sync vào Chat)")
    @PostMapping(Endpoint.Group.MEMBERS_BASE)
    public ApiResponse<Void> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddMemberRequest request) {

        // Service bắn Event MemberJoined -> Tự thêm vào Chat
        // Giả sử request.getUserId() trả về UUID của người được thêm
//        groupService.addMemberToGroup(groupId, request.getUserId());

        return ApiResponse.<Void>builder()
                .message("Member added successfully and syncing to chat...")
                .build();
    }

    @Operation(summary = "Lấy danh sách thành viên nhóm")
    @GetMapping(Endpoint.Group.MEMBERS_BASE)
    public ApiResponse<List<GroupMemberResponse>> getMembers(@PathVariable UUID groupId) {
        return ApiResponse.<List<GroupMemberResponse>>builder()
//                .data(groupService.getMembers(groupId))
                .build();
    }

    @Operation(summary = "Xóa thành viên khỏi nhóm (Tự động kick khỏi Chat)")
    @DeleteMapping(Endpoint.Group.MEMBERS_ID)
    public ApiResponse<Void> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId) {
//        groupService.removeMember(groupId, memberId);
        return ApiResponse.<Void>builder().message("Member removed successfully").build();
    }

    @Operation(summary = "Cập nhật vai trò thành viên (Leader/Member)")
    @PutMapping(Endpoint.Group.MEMBERS_ID)
    public ApiResponse<GroupMemberResponse> updateMemberRole(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        return ApiResponse.<GroupMemberResponse>builder()
//                .data(groupService.updateMemberRole(groupId, memberId, request))
                .build();
    }

    // --- LOCATION SUGGESTIONS ---

    @Operation(summary = "Đề xuất địa điểm cho nhóm")
    @PostMapping(Endpoint.Group.LOCATION_SUGGESTIONS)
    public ApiResponse<SuggestLocationResponse> suggestLocation(
            @PathVariable UUID groupId,
            @Valid @RequestBody SuggestLocationRequest request) {
        return ApiResponse.<SuggestLocationResponse>builder()
//                .data(groupService.suggestLocation(groupId, request))
                .build();
    }

    @Operation(summary = "Lấy danh sách địa điểm được đề xuất")
    @GetMapping(Endpoint.Group.LOCATION_SUGGESTIONS)
    public ApiResponse<List<SuggestLocationResponse>> getSuggestedLocations(@PathVariable UUID groupId) {
        return ApiResponse.<List<SuggestLocationResponse>>builder()
//                .data(groupService.getSuggestedLocations(groupId))
                .build();
    }
}