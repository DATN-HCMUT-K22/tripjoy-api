package com.tripjoy.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_chat_message_cursor", columnList = "conversation_id, created_at DESC")
})
public class ChatMessage extends BaseEntity {

    private String messageType;
    private String messageContent;
    private String mediaUrl;
    private String sharedPostUrl;
    private Boolean isBot;
    private String status;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPinned = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id", referencedColumnName = "id")
    private ChatMessage parentMessage;

    @OneToMany(mappedBy = "parentMessage", fetch = FetchType.LAZY)
    private Set<ChatMessage> replies = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation; // Mỗi tin nhắn thuộc về 1 conversation

    @ManyToMany
    @JoinTable(name = "like_chat_message", joinColumns = @JoinColumn(name = "chat_message_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private Set<User> likeUsers = new HashSet<>();
}
