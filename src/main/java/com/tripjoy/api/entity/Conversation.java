package com.tripjoy.api.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tripjoy.api.enums.ConversationType;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "conversation",
        indexes = {
            @Index(name = "idx_conversation_group", columnList = "group_id"),
            @Index(name = "idx_conversation_timestamp", columnList = "last_message_timestamp DESC")
        })
public class Conversation extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private ConversationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    private String name;

    private LocalDateTime lastMessageTimestamp;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ChatMessage> messages;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ConversationMember> members;
}
