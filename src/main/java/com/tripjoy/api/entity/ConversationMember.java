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