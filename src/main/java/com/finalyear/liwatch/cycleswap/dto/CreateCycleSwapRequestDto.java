package com.finalyear.liwatch.cycleswap.dto;

import lombok.Data;

@Data
public class CreateCycleSwapRequestDto {
    private Long middlemanId;
    private Long closerId;
    private Long postAId;
    private Long postBId;
    private Long postCId;
}
