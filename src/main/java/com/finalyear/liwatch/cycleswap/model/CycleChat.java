package com.finalyear.liwatch.cycleswap.model;

import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cycle_chats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycleChat {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_negotiation_id")
    private CycleNegotiation cycleNegotiation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime sentAt;
}
