package com.tripjoy.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class ChatMessage extends BaseEntity{

    private String messageType;
    private String messageContent;
    private String mediaUrl;
    private String sharedPostUrl;
    private Boolean isBot;
    private String status;
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_message_id", referencedColumnName = "id")
    private ChatMessage replyMessage;

    @OneToMany(mappedBy = "replyMessage", fetch = FetchType.LAZY)
    private Set<ChatMessage> replies = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = true)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @ManyToMany
    @JoinTable(
            name = "like_chat_message",
            joinColumns = @JoinColumn(name = "chat_message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likeUsers = new HashSet<>();
}
