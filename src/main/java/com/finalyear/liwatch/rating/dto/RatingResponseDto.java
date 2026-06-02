package com.finalyear.liwatch.rating.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RatingResponseDto {
    private Long ratingId;
    private Long fromUserId;
    private String fromUserName;
    private String fromUserProfileImage;
    private Integer score;
    private String comment;
    private LocalDateTime publishedAt;
}
