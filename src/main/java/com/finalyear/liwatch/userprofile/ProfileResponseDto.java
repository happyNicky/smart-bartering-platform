package com.finalyear.liwatch.userprofile;

import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userprofile.enums.BadgeLevel;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponseDto {
    private Long profileId;
    private String location;
    private String bio;
    private BigDecimal trustScore;
    private BadgeLevel badgeLevel;
    private String badgeLabel;
    private String profileImage;
    private UserSummeryDto user;
    private List<PublishedReviewDto> reviews;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PublishedReviewDto {
        private Long ratingId;
        private Long fromUserId;
        private String fromUserName;
        private Integer score;
        private String comment;
        private java.time.LocalDateTime publishedAt;
    }
}
