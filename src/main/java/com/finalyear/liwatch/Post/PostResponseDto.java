package com.finalyear.liwatch.Post;

import com.finalyear.liwatch.Item.ItemRequestDto;
import com.finalyear.liwatch.Post.enums.ExchangeType;
import com.finalyear.liwatch.Post.enums.PostType;
import com.finalyear.liwatch.Post.enums.Status;
import com.finalyear.liwatch.media.postMedia.PostMedia;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
import com.finalyear.liwatch.service.ServiceRequestDto;
import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userManagement.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long postId;
    private String title;
    private String description;
    private String category;
    private ExchangeType exchangeType;
    private Status status;
    private PostType postType;
    private LocalDateTime createdAt;
    private List<PostMediaDto> postImages;
    private UserSummeryDto user;
    private ItemRequestDto item;
    private ServiceRequestDto service;

    public PostResponseDto(Long postId, String title, String description, String category, ExchangeType exchangeType, Status status, PostType postType, LocalDateTime createdAt, List<PostMediaDto> mediaDtos, UserSummeryDto userDto, ItemRequestDto itemRequestDto) {
        this.postId = postId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.exchangeType = exchangeType;
        this.status = status;
        this.postType = postType;
        this.createdAt = createdAt;
        this.postImages=mediaDtos;
        this.user=userDto;
        this.item=itemRequestDto;
    }

    public PostResponseDto(Long postId, String title, String description, String category, ExchangeType exchangeType, Status status, PostType postType, LocalDateTime createdAt, List<PostMediaDto> mediaDtos, UserSummeryDto userDto, ServiceRequestDto serviceRequestDto) {
        this.postId = postId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.exchangeType = exchangeType;
        this.status = status;
        this.postType = postType;
        this.createdAt = createdAt;
        this.postImages=mediaDtos;
        this.user=userDto;
        this.service=serviceRequestDto;
    }
}
