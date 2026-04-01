package com.finalyear.liwatch.Post;

import com.finalyear.liwatch.Post.enums.ExchangeType;
import com.finalyear.liwatch.Post.enums.Status;
import com.finalyear.liwatch.media.postMedia.PostMedia;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
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
    private Long posId;
    private String title;
    private String description;
    private String category;
    private ExchangeType exchangeType;
    private Status status;
    private LocalDateTime createdAt;
    private List<PostMediaDto> postImages;
    private UserSummeryDto user;
}
