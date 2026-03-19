package com.finalyear.liwatch.chat;

import com.finalyear.liwatch.negotiation.Negotiation;
import com.finalyear.liwatch.userManagement.model.User;
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

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Negotiation negotiation;

    @ManyToOne
    private User sender;



    @Column(columnDefinition = "TEXT")
    private String messageText;

    private boolean isEncrypted;

    private LocalDateTime sentAt;
}
