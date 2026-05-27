package com.finalyear.liwatch.trust.dto;

import com.finalyear.liwatch.userprofile.enums.BadgeLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BadgeHistoryDto {
    private Long badgeId;
    private BadgeLevel badgeLevel;
    private String badgeLabel;
    private BigDecimal trustScoreAtAward;
    private Integer totalSwaps;
    private BigDecimal averageRating;
    private Integer reportCount;
    private LocalDateTime awardedAt;
}
