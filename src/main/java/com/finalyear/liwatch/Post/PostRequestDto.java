package com.finalyear.liwatch.Post;

import com.finalyear.liwatch.Item.ItemRequestDto;
import com.finalyear.liwatch.Post.enums.ExchangeType;
import com.finalyear.liwatch.Post.enums.PostType;
import com.finalyear.liwatch.Post.enums.Status;
import com.finalyear.liwatch.media.postMedia.PostMedia;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
import com.finalyear.liwatch.service.ServiceRequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {
    private String title;
    private String description;
    private String category;
    private PostType postType=PostType.ITEM;
    private ExchangeType exchangeType;
    private List<PostMediaDto> postImages;

    private ItemRequestDto item;
    private ServiceRequestDto service;

}
