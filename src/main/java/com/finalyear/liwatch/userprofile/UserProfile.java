package com.finalyear.liwatch.userprofile;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "user_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "trust_score", precision = 3, scale = 2)
    private BigDecimal trustScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_level")
    private BadgeLevel badgeLevel;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    public enum BadgeLevel {
        NEW,
        TRUSTED
    }
}