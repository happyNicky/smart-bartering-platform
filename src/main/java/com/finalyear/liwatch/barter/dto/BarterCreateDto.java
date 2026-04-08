package com.finalyear.liwatch.barter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarterCreateDto {

    @NotNull
    private Long swapRequestId;

    @NotNull
    private Long userAId;

    @NotNull
    private Long userBId;

    @NotNull
    private Long postAId;

    @NotNull
    private Long postBId;
}
