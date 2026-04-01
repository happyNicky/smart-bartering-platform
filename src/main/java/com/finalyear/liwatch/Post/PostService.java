package com.finalyear.liwatch.Post;

import com.finalyear.liwatch.Post.enums.Status;
import com.finalyear.liwatch.media.postMedia.PostMedia;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
import com.finalyear.liwatch.media.postMedia.PostMediaRepository;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class PostService {

   // injection
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostMediaRepository postMediaRepository;


    public Post createPost(PostRequestDto postItem) {

        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String username = authentication.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // create a new post and set data from the post request dto
        Post post = new Post();
        post.setTitle(postItem.getTitle());
        post.setDescription(postItem.getDescription());
        post.setCreatedAt(LocalDateTime.now());
        post.setCategory(postItem.getCategory());
        post.setExchangeType(postItem.getExchangeType());
        post.setStatus(Status.ACTIVE);
        post.setUser(user);


      // Convert image URLs to PostMedia entities
        List<PostMedia> postMediaList= new ArrayList<>();
        Post savedPost = postRepository.save(post);

        for(PostMediaDto media : postItem.getPostImages())
        {
            PostMedia postMedia= new PostMedia();
            postMedia.setPost(savedPost);
            postMedia.setPostImageUrl(media.getPostImageUrl());
            PostMedia med= postMediaRepository.save(postMedia);
            postMediaList.add(med);
        }


        savedPost.getPostImages().clear();
        savedPost.getPostImages().addAll(postMediaList);

// Save the post with its media
        return postRepository.save(post);
    }


    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}
