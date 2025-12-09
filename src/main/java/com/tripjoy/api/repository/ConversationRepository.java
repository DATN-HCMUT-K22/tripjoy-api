package com.tripjoy.api.repository;

import com.tripjoy.api.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByGroup_Id(UUID groupId);

    @Query("SELECT c FROM Conversation c JOIN c.members m WHERE m.user.id = :userId")
    List<Conversation> findAllByUserId(@Param("userId") UUID userId);
}
