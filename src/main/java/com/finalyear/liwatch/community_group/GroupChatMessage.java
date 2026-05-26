package com.finalyear.liwatch.community_group;

import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_chat_messages", indexes = {
        @Index(name = "idx_group_id", columnList = "group_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String messageText;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
