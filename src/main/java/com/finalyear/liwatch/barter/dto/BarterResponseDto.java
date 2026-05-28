package com.finalyear.liwatch.barter.dto;
import com.finalyear.liwatch.Post.PostResponseDto;
import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarterResponseDto {

    private Long id;
    private LocalDateTime createdAt;

    private Long swapRequestId;

    private UserSummeryDto userA;
    private UserSummeryDto userB;

    private PostResponseDto postA;
    private PostResponseDto postB;
}