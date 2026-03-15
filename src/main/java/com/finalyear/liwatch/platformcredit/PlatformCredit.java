package com.finalyear.liwatch.platformcredit;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "platform_credits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_id")
    private Long creditId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(name = "earned_from", nullable = false, length = 150)
    private String earnedFrom;
}
