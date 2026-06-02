package com.finalyear.liwatch.cycleswap.dto;

import com.finalyear.liwatch.Post.PostResponseDto;
import com.finalyear.liwatch.directswap.request_enum.RequestStatus;
import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleSwapRequestResponseDto {
    private Long id;
    private UserSummeryDto initiator;
    private UserSummeryDto middleman;
    private UserSummeryDto closer;
    
    private PostResponseDto postA;
    private PostResponseDto postB;
    private PostResponseDto postC;

    private boolean middlemanAccepted;
    private boolean closerAccepted;

    private RequestStatus status;
    private LocalDateTime createdAt;
    private Long cycleBarterId;
}
