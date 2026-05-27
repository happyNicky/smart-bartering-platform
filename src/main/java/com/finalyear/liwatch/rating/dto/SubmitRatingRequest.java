package com.finalyear.liwatch.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitRatingRequest {

    @NotNull
    private Long barterId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;

    private String comment;
}
