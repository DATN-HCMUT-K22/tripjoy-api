package com.tripjoy.api.service.impl;

import com.tripjoy.api.dto.event.GroupCreatedEvent;
import com.tripjoy.api.dto.event.MemberJoinedGroupEvent;
import com.tripjoy.api.dto.request.GroupRequest;
import com.tripjoy.api.dto.response.GroupResponse;
import com.tripjoy.api.dto.response.simple.GroupMemberResponse;
import com.tripjoy.api.entity.*;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.GroupMapper;
import com.tripjoy.api.repository.*;
import com.tripjoy.api.service.IGroupService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupService implements IGroupService {

    GroupRepository groupRepository;
    GroupMemberRepository groupMemberRepository;
    UserRepository userRepository;
    ApplicationEventPublisher eventPublisher;

    // Inject Mapper
    GroupMapper groupMapper;

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(UUID groupId) {
        return groupRepository.findById(groupId)
                .map(groupMapper::toGroupResponse)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups(UUID userId) {
        List<GroupMember> memberRecords = groupMemberRepository.findByUserId(userId);

        return memberRecords.stream()
                .map(GroupMember::getGroup)
                .map(groupMapper::toGroupResponse)
                .toList();
    }

    @Transactional
    public GroupResponse createGroup(GroupRequest request, UUID ownerId) {
        // --- STEP 0: FETCH OWNER ---
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // --- STEP 1: MAP REQUEST -> ENTITY ---
        Group group = groupMapper.toGroup(request);
        // Set default logic...
        groupRepository.save(group);

        // --- STEP 2: ADD OWNER (LEADER) ---
        GroupMember ownerMember = GroupMember.builder()
                .group(group)
                .user(owner)
                .isLeader(true)
                .build();
        groupMemberRepository.save(ownerMember);

        // --- STEP 3: HANDLE INITIAL MEMBERS (Mời thêm bạn bè ngay lúc tạo) ---
        List<User> initialMembers = new ArrayList<>();

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            // 3.1 Lấy danh sách User từ DB theo List ID
            initialMembers = userRepository.findAllById(request.getMemberIds());

            // 3.2 Lưu vào bảng GroupMember
            for (User member : initialMembers) {
                // Tránh add trùng Owner nếu client lỡ gửi ID của owner lên
                if (member.getId().equals(owner.getId()))
                    continue;

                GroupMember groupMember = GroupMember.builder()
                        .group(group)
                        .user(member)
                        .isLeader(false)
                        .build();
                groupMemberRepository.save(groupMember);
            }
        }

        // --- STEP 4: FIRE EVENT (Sửa lỗi ở đây) ---
        // Truyền đủ 3 tham số: group, creator, và list thành viên ban đầu
        eventPublisher.publishEvent(new GroupCreatedEvent(group, owner, initialMembers));

        // --- STEP 5: MAP ENTITY -> RESPONSE ---
        return groupMapper.toGroupResponse(group);
    }

    @Transactional
    public GroupMemberResponse addMemberToGroup(UUID groupId, UUID userId) {
        // --- STEP 1: VALIDATION ---
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check exists (Optional)
        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new AppException(ErrorCode.USER_ALREADY_IN_GROUP);
        }

        // --- STEP 2: SAVE MEMBER ---
        GroupMember gMember = GroupMember.builder()
                .group(group)
                .user(user)
                .isLeader(false) // Mặc định vào là member thường
                .build();

        // Lưu xong mới có ID, createdAt...
        GroupMember savedMember = groupMemberRepository.save(gMember);

        // --- STEP 3: FIRE EVENT ---
        eventPublisher.publishEvent(new MemberJoinedGroupEvent(group, user));

        // --- STEP 4: MAP ENTITY -> RESPONSE ---
        return groupMapper.toGroupMemberResponse(savedMember);
    }
}