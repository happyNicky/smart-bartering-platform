package com.finalyear.liwatch.directswap.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDirectSwapRequestDto {
    @NotNull
    private Long receiverId;
    @NotNull
    private Long offeredPostId;
    @NotNull
    private Long requestedPostId;
}
