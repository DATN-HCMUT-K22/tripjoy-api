package com.tripjoy.api.entity;

import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "conversation_member",
        indexes = {
            @Index(name = "idx_conversation_member_lookup", columnList = "conversation_id, user_id"),
            @Index(name = "idx_user_conversations", columnList = "user_id")
        })
public class ConversationMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    private Long unreadCount = 0L;

    @Builder.Default
    private Boolean isMuted = false;

    // pin conversation to top
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPinned = false;

    private UUID lastReadMessageId;
}
