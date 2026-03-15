package com.finalyear.liwatch.chat;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "negotiation_id", nullable = false)
    private Long negotiationId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "is_encrypted")
    private Boolean isEncrypted = true;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
