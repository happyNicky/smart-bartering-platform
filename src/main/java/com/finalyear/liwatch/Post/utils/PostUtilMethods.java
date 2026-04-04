package com.finalyear.liwatch.Post.utils;

import com.finalyear.liwatch.Item.Item;
import com.finalyear.liwatch.Item.ItemRequestDto;
import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.PostRequestDto;
import com.finalyear.liwatch.Post.PostResponseDto;
import com.finalyear.liwatch.Post.enums.Status;
import com.finalyear.liwatch.media.postMedia.PostMedia;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
import com.finalyear.liwatch.service.Service;
import com.finalyear.liwatch.service.ServiceRequestDto;
import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userManagement.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostUtilMethods {

    public static PostResponseDto getPostResponseDtoFromPost(User user, Post post, List<PostMediaDto> postMediaDtosList)
    {
        UserSummeryDto usd=new UserSummeryDto(user.getId(),user.getFullName(), user.getEmail());
        PostResponseDto prd= new PostResponseDto();
        prd.setPostId(post.getPostId());
        prd.setTitle(post.getTitle());
        prd.setDescription(post.getDescription());
        prd.setStatus(post.getStatus());
        prd.setPostType(post.getPostType());
        prd.setExchangeType(post.getExchangeType());
        prd.setCategory(post.getCategory());
        prd.setPostImages(postMediaDtosList);
        prd.setUser(usd);
        prd.setCreatedAt(post.getCreatedAt());
        return prd;
    }



    public static Item createItemFromRequest(PostRequestDto postRequestDto)
    {
        Item item= new Item();

        //common fields
        item.setTitle(postRequestDto.getTitle());
        item.setDescription(postRequestDto.getDescription());
        item.setCategory(postRequestDto.getCategory());
        item.setExchangeType(postRequestDto.getExchangeType());
        item.setPostType(postRequestDto.getPostType());
        item.setCondition(postRequestDto.getItem().getCondition());

        item.setStatus(Status.ACTIVE);


        item.setPartialCashAllowed(postRequestDto.getItem().getPartialCashAllowed());
        item.setEstimatedValue(postRequestDto.getItem().getEstimatedValue());
        item.setCondition(postRequestDto.getItem().getCondition());
        return item;
    }
    public static Service createServiceFromRequest(PostRequestDto postRequestDto)
    {
        Service service= new Service();

        //common fields
        service.setTitle(postRequestDto.getTitle());
        service.setDescription(postRequestDto.getDescription());
        service.setCategory(postRequestDto.getCategory());
        service.setExchangeType(postRequestDto.getExchangeType());
        service.setPostType(postRequestDto.getPostType());
        service.setStatus(Status.ACTIVE);


        service.setSkillLevel(postRequestDto.getService().getSkillLevel());
        service.setAvailability(postRequestDto.getService().getAvailability());
        service.setServiceDuration(postRequestDto.getService().getServiceDuration());
        return service;
    }

    public static ItemRequestDto createItemResponseDtoFromItem(Item item)
    {
        ItemRequestDto itemResponseDto= new ItemRequestDto();
        itemResponseDto.setPartialCashAllowed(item.getPartialCashAllowed());
        itemResponseDto.setCondition(item.getCondition());
        itemResponseDto.setEstimatedValue(item.getEstimatedValue());
        return itemResponseDto;
    }
    public static ServiceRequestDto createServiceResponseDtoFromService(Service service)
    {
        ServiceRequestDto serviceResponseDto= new ServiceRequestDto();
        serviceResponseDto.setServiceDuration(service.getServiceDuration());
        serviceResponseDto.setAvailability(service.getAvailability());
        serviceResponseDto.setSkillLevel(service.getSkillLevel());
        return serviceResponseDto;
    }

}
