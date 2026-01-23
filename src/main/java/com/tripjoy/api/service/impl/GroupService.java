package com.tripjoy.api.service.impl;

import com.tripjoy.api.dto.event.GroupCreatedEvent;
import com.tripjoy.api.dto.event.MemberJoinedGroupEvent;
import com.tripjoy.api.dto.event.MemberRemovedFromGroupEvent;
import com.tripjoy.api.dto.request.GroupRequest;
import com.tripjoy.api.dto.request.member.TransferLeadershipRequest;
import com.tripjoy.api.dto.request.member.UpdateMemberRoleRequest;
import com.tripjoy.api.dto.response.GroupResponse;
import com.tripjoy.api.dto.response.GroupMemberResponse;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupService implements IGroupService {

        GroupRepository groupRepository;
        GroupMemberRepository groupMemberRepository;
        ItineraryRepository itineraryRepository;
        ConversationRepository conversationRepository;
        ConversationMemberRepository conversationMemberRepository;
        UserRepository userRepository;
        ApplicationEventPublisher eventPublisher;
        @PersistenceContext
        EntityManager entityManager;

        // Inject Mapper
        GroupMapper groupMapper;

        @Transactional(readOnly = true)
        public GroupResponse getGroupById(UUID groupId) {
                return groupRepository.findByIdAndNotDeleted(groupId)
                                .map(groupMapper::toGroupResponse)
                                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));
        }

        @Transactional(readOnly = true)
        public List<GroupResponse> getMyGroups(UUID userId) {
                // Use NOT DELETED query to filter soft-deleted memberships
                List<GroupMember> memberRecords = groupMemberRepository.findByUserIdAndNotDeleted(userId);

                return memberRecords.stream()
                                .map(GroupMember::getGroup)
                                .filter(group -> !group.getSoftDeleteInfo().isDeleted()) // Also filter deleted groups
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

        @Override
        @Transactional(readOnly = true)
        public List<GroupMemberResponse> getGroupMembers(UUID groupId) {
                // Verify group exists
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

                // Get all members from repository
                List<GroupMember> members = groupMemberRepository.findByGroupOrderByRoleAsc(group);

                // Map to response DTOs
                return members.stream()
                                .map(groupMapper::toGroupMemberResponse)
                                .toList();
        }

        @Override
        @Transactional
        public void removeMemberFromGroup(UUID groupId, UUID memberId, UUID currentUserId) {
                // 1. Validate Group & Permissions (Giữ nguyên code của bạn)
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

                User currentUser = userRepository.findById(currentUserId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                if (!groupMemberRepository.hasLeadershipRole(group, currentUser)) {
                        throw new AppException(ErrorCode.UNAUTHORIZED);
                }

                // 2. Find member (Giữ nguyên)
                GroupMember memberToRemove = groupMemberRepository.findById(memberId)
                                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

                // 3. Validations (Giữ nguyên)
                if (memberToRemove.getUser().getId().equals(currentUserId)) {
                        throw new AppException(ErrorCode.CANNOT_REMOVE_YOURSELF);
                }
                if (memberToRemove.getRole() == GroupRole.LEADER) {
                        throw new AppException(ErrorCode.CANNOT_REMOVE_LEADER);
                }

                groupMemberRepository.delete(memberToRemove);

                // 4. Publish event (Giữ nguyên)
                eventPublisher.publishEvent(new MemberRemovedFromGroupEvent(
                                group,
                                memberToRemove.getUser(),
                                currentUser));
        }

        @Override
        @Transactional
        public void leaveGroup(UUID groupId, UUID currentUserId) {
                // 1. Validate Group exists
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

                // 2. Find current user's membership
                User currentUser = userRepository.findById(currentUserId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                // 3. Find member record
                GroupMember memberToLeave = groupMemberRepository.findByGroupAndUser(group, currentUser)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_IN_GROUP));

                // 4. Business Rule: LEADER cannot leave (must transfer leadership first)
                if (memberToLeave.getRole() == GroupRole.LEADER) {
                        throw new AppException(ErrorCode.LEADER_CANNOT_LEAVE);
                }

                // 5. Delete membership
                groupMemberRepository.delete(memberToLeave);

                // 6. Publish event for chat synchronization (same as remove)
                eventPublisher.publishEvent(new MemberRemovedFromGroupEvent(
                                group,
                                currentUser,
                                currentUser // Self-initiated leave
                ));
        }

        @Override
        @Transactional
        public GroupMemberResponse updateMemberRole(UUID groupId, UUID memberId, UpdateMemberRoleRequest request,
                        UUID currentUserId) {
                // 1. Validate Group exists
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

                // 2. Verify current user is LEADER
                User currentUser = userRepository.findById(currentUserId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_IN_GROUP));

                if (currentMember.getRole() != GroupRole.LEADER) {
                        throw new AppException(ErrorCode.ONLY_LEADER_ALLOWED);
                }

                // 3. Find member to update
                GroupMember memberToUpdate = groupMemberRepository.findById(memberId)
                                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

                // 4. Business Rules
                // Cannot change LEADER role
                if (memberToUpdate.getRole() == GroupRole.LEADER) {
                        throw new AppException(ErrorCode.CANNOT_CHANGE_LEADER_ROLE);
                }

                // Cannot assign LEADER role (use transfer leadership instead)
                if (request.getRole() == GroupRole.LEADER) {
                        throw new AppException(ErrorCode.CANNOT_ASSIGN_LEADER_ROLE);
                }

                // 5. Update role
                memberToUpdate.setRole(request.getRole());
                GroupMember updated = groupMemberRepository.save(memberToUpdate);

                return groupMapper.toGroupMemberResponse(updated);
        }

        @Override
        @Transactional
        public void transferLeadership(UUID groupId, TransferLeadershipRequest request, UUID currentUserId) {
                // 1. Validate Group exists
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

                // 2. Verify current user is LEADER
                User currentUser = userRepository.findById(currentUserId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                GroupMember currentLeader = groupMemberRepository.findByGroupAndUser(group, currentUser)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_IN_GROUP));

                if (currentLeader.getRole() != GroupRole.LEADER) {
                        throw new AppException(ErrorCode.ONLY_LEADER_ALLOWED);
                }

                // 3. Validate new leader exists and is a member
                User newLeaderUser = userRepository.findById(request.getNewLeaderId())
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                GroupMember newLeader = groupMemberRepository.findByGroupAndUser(group, newLeaderUser)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_IN_GROUP));

                // 4. Cannot transfer to yourself
                if (currentUserId.equals(request.getNewLeaderId())) {
                        throw new AppException(ErrorCode.CANNOT_TRANSFER_TO_YOURSELF);
                }

                // 5. Swap roles
                // Old LEADER -> CO_LEADER
                currentLeader.setRole(GroupRole.CO_LEADER);
                groupMemberRepository.save(currentLeader);

                // New member -> LEADER
                newLeader.setRole(GroupRole.LEADER);
                groupMemberRepository.save(newLeader);
        }

        // === CASCADE SOFT DELETE METHODS ===

        @Override
        @Transactional
        public void deleteGroup(UUID groupId, UUID currentUserId) {
                // 1. Validate Group exists
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

                // 2. Verify current user is LEADER
                User currentUser = userRepository.findById(currentUserId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                GroupMember currentLeader = groupMemberRepository.findByGroupAndUser(group, currentUser)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_IN_GROUP));

                if (currentLeader.getRole() != GroupRole.LEADER) {
                        throw new AppException(ErrorCode.ONLY_LEADER_ALLOWED);
                }

                LocalDateTime now = LocalDateTime.now();
                String deletedBy = currentUserId.toString();

                // 3. Soft delete Group
                group.getSoftDeleteInfo().markAsDeleted(deletedBy);
                groupRepository.save(group);

                // 4. Cascade delete related entities
                // Soft delete: GroupMember, Itinerary (keeping travel history)
                groupMemberRepository.softDeleteByGroupId(groupId, now, deletedBy);
                itineraryRepository.softDeleteByGroupId(groupId, now, deletedBy);

                // HARD DELETE: Conversations & Messages (no history needed)
                // JPA cascade with orphanRemoval=true will auto-delete:
                // - ConversationMember (via Conversation.members)
                // - ChatMessage (via Conversation.messages)
                List<Conversation> conversations = conversationRepository.findByGroup_Id(groupId);
                conversationRepository.deleteAll(conversations);
        }

        @Override
        @Transactional
        public void restoreGroup(UUID groupId, UUID currentUserId) {
                // 1. Validate Group exists (even if deleted)
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

                // 2. Check if group is actually deleted
                if (!group.getSoftDeleteInfo().isDeleted()) {
                        throw new AppException(ErrorCode.GROUP_NOT_DELETED);
                }

                // 3. Verify current user was a member (check in soft deleted members)
                User currentUser = userRepository.findById(currentUserId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_IN_GROUP));

                // Only LEADER can restore
                if (currentMember.getRole() != GroupRole.LEADER) {
                        throw new AppException(ErrorCode.ONLY_LEADER_ALLOWED);
                }

                // 4. Restore Group
                group.getSoftDeleteInfo().restore();
                groupRepository.save(group);

                // 5. Cascade restore travel-related entities
                groupMemberRepository.restoreByGroupId(groupId);
                itineraryRepository.restoreByGroupId(groupId);

                // Note: Conversations were HARD DELETED - cannot restore
                // Group restoration will NOT bring back old chats
        }
}