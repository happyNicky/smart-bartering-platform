package com.finalyear.liwatch.trust.dto;

import com.finalyear.liwatch.userprofile.enums.BadgeLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TrustResponseDto {
    private Long userId;
    private BigDecimal trustScore;
    private BadgeLevel badgeLevel;
    private String badgeLabel;
    private BigDecimal averageRating;
}
