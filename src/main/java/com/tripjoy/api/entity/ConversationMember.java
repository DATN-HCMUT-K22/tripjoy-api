package com.tripjoy.api.entity;

import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conversation_member", indexes = {
        @Index(name = "idx_conversation_member_lookup", columnList = "conversation_id, user_id"),
        @Index(name = "idx_user_conversations", columnList = "user_id, is_deleted")
})
public class ConversationMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Long unreadCount = 0L;

    private Boolean isMuted = false;

    // pin conversation to top
    @Column(nullable = false)
    private Boolean isPinned = false;

    private UUID lastReadMessageId;

    @Embedded
    @Builder.Default
    private SoftDeleteInfo softDeleteInfo = new SoftDeleteInfo();
}