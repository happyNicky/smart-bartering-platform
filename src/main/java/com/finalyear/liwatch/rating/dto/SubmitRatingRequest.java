package com.finalyear.liwatch.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitRatingRequest {

    private Long barterId;
    
    private Long cycleBarterId;

    private Long toUserId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;

    private String comment;
}
