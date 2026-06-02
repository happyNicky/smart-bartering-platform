package com.finalyear.liwatch.admin;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_action_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;
}
