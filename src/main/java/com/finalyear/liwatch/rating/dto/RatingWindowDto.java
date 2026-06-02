package com.finalyear.liwatch.rating.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RatingWindowDto {
    private Long windowId;
    private String status; // "Open", "Published", "Expired", or null
    private LocalDateTime deadline;
    private Boolean userSubmitted;
    private Boolean otherUserSubmitted;
    
    // Current user's rating details (for viewing/editing own review)
    private Long myRatingId;
    private Integer myScore;
    private String myComment;

    // Partner's rating details (only available once published/expired)
    private Integer partnerScore;
    private String partnerComment;
}
