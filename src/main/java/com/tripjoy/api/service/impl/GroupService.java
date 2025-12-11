package com.tripjoy.api.service.impl;

import com.tripjoy.api.dto.event.GroupCreatedEvent;
import com.tripjoy.api.dto.event.MemberJoinedGroupEvent;
import com.tripjoy.api.dto.request.GroupRequest;
import com.tripjoy.api.dto.response.GroupResponse;
import com.tripjoy.api.dto.response.GroupMemberResponse;
import com.tripjoy.api.entity.*;
import com.tripjoy.api.enums.GroupRole;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.GroupMapper;
import com.tripjoy.api.repository.*;
import com.tripjoy.api.service.IGroupService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    @PersistenceContext
    EntityManager entityManager;

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

        // Init collections if null (vì @Builder không init được)
        if (group.getMembers() == null)
            group.setMembers(new java.util.HashSet<>());
        if (group.getItineraries() == null)
            group.setItineraries(new java.util.HashSet<>());
        if (group.getConversations() == null)
            group.setConversations(new java.util.HashSet<>());

        // Set default logic...
        groupRepository.save(group);

        // --- STEP 2: ADD OWNER (LEADER) ---
        GroupMember ownerMember = GroupMember.builder()
                .group(group)
                .user(owner)
                .role(GroupRole.LEADER)
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
                        .role(GroupRole.MEMBER)
                        .build();
                groupMemberRepository.save(groupMember);
            }
        }

        // [REFRESH] Để lấy lại danh sách member chuẩn từ DB
        entityManager.flush();
        entityManager.refresh(group);

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
                .role(GroupRole.MEMBER)
                .build();

        // Lưu xong mới có ID, createdAt...
        GroupMember savedMember = groupMemberRepository.save(gMember);

        // --- STEP 3: FIRE EVENT ---
        eventPublisher.publishEvent(new MemberJoinedGroupEvent(group, user));

        // --- STEP 4: MAP ENTITY -> RESPONSE ---
        return groupMapper.toGroupMemberResponse(savedMember);
    }

    @Transactional
    public GroupResponse updateGroup(UUID groupId, GroupRequest request, UUID currentUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check if user has LEADER or CO_LEADER role
        if (!groupMemberRepository.hasLeadershipRole(group, currentUser)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Update group fields using mapper
        groupMapper.updateGroup(group, request);
        Group updated = groupRepository.save(group);

        return groupMapper.toGroupResponse(updated);
    }
}