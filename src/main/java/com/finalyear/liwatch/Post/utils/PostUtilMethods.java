package com.finalyear.liwatch.Post.utils;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.PostResponseDto;
import com.finalyear.liwatch.media.postMedia.PostMedia;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userManagement.model.User;

import java.util.ArrayList;
import java.util.List;

public class PostUtilMethods {

    public static PostResponseDto getPostResponseDtoFromPost(User user, Post post, List<PostMediaDto> postMediaDtosList)
    {
        UserSummeryDto usd=new UserSummeryDto(user.getId(),user.getFullName(), user.getEmail());
        PostResponseDto prd= new PostResponseDto();
        prd.setPosId(post.getPostId());
        prd.setTitle(post.getTitle());
        prd.setDescription(post.getDescription());
        prd.setStatus(post.getStatus());
        prd.setExchangeType(post.getExchangeType());
        prd.setCategory(post.getCategory());
        prd.setPostImages(postMediaDtosList);
        prd.setUser(usd);
        prd.setCreatedAt(post.getCreatedAt());

        return prd;
    }


}
