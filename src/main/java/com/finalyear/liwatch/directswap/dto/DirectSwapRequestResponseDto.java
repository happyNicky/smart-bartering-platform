package com.finalyear.liwatch.directswap.dto;

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
public class DirectSwapRequestResponseDto {
    private Long id;
    private UserSummeryDto requestSender;
    private UserSummeryDto requestReceiver;
    private PostResponseDto offeredPost;
    private PostResponseDto requestedPost;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private Long barterId;
}