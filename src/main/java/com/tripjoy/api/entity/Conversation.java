package com.tripjoy.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;
import com.tripjoy.api.enums.ConversationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private ConversationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    private String name;

    private LocalDateTime lastMessageTimestamp;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ChatMessage> messages;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ConversationMember> members;

    @Embedded
    @Builder.Default
    private SoftDeleteInfo softDeleteInfo = new SoftDeleteInfo();
}
