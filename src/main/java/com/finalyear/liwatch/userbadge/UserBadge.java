package com.finalyear.liwatch.userbadge;


import com.finalyear.liwatch.userprofile.enums.BadgeLevel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_badge")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long badgeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_level", nullable = false)
    private BadgeLevel badgeLevel;

    @Column(name = "total_swaps")
    private Integer totalSwaps = 0;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "report_count")
    private Integer reportCount = 0;

    @Column(name = "awarded_at", nullable = false)
    private LocalDateTime awardedAt;


}