package com.finalyear.liwatch.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateRatingRequest {

    @Min(1)
    @Max(5)
    private Integer score;

    private String comment;
}
