package com.tripjoy.api.repository;

import com.tripjoy.api.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

        List<Conversation> findByGroup_Id(UUID groupId);

        @Query("SELECT DISTINCT c FROM Conversation c " +
                        "LEFT JOIN FETCH c.members m " +
                        "LEFT JOIN FETCH m.user " +
                        "LEFT JOIN FETCH c.group g " +
                        "JOIN GroupMember gm ON gm.group.id = g.id AND gm.user.id = m.user.id " +
                        "WHERE m.user.id = :userId " +
                        "AND g.softDeleteInfo.isDeleted = false " +
                        "AND gm.softDeleteInfo.isDeleted = false")
        List<Conversation> findAllByUserId(@Param("userId") UUID userId);
}
