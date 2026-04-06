package com.finalyear.liwatch.barter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarterResponseDto {

    private Long id;
    private LocalDateTime createdAt;

    private Long swapRequestId;

    private Long userAId;
    private Long userBId;

    private Long postAId;
    private Long postBId;
}