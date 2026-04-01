package com.finalyear.liwatch.Post;

import com.finalyear.liwatch.Post.enums.Status;
import com.finalyear.liwatch.Post.utils.PostUtilMethods;
import com.finalyear.liwatch.media.postMedia.PostMedia;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
import com.finalyear.liwatch.media.postMedia.PostMediaRepository;
import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class PostService {

   // injection
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostMediaRepository postMediaRepository;
    @Autowired
    private UserUtilMethods userUtilMethods;




    // create a single post
    public PostResponseDto createPost(PostRequestDto postItem) {

        //get currently authenticated user
        User user= userUtilMethods.getCurrentlyAuthenticatedUser();

        // create a new post and set data from the post request dto
        Post post = new Post();
        post.setTitle(postItem.getTitle());
        post.setDescription(postItem.getDescription());
        post.setCreatedAt(LocalDateTime.now());
        post.setCategory(postItem.getCategory());
        post.setExchangeType(postItem.getExchangeType());
        post.setStatus(Status.ACTIVE);
        post.setUser(user);


        Post savedPost = postRepository.save(post);

      // Convert image URLs to PostMedia entities
        List<PostMedia> postMediaList= new ArrayList<>();
        List<PostMediaDto> postMediaDtosList= new ArrayList<>();

        for(PostMediaDto media : postItem.getPostImages())
        {
            PostMedia postMedia= new PostMedia();
            postMedia.setPost(savedPost);
            postMedia.setPostImageUrl(media.getPostImageUrl());
            postMedia= postMediaRepository.save(postMedia);
            postMediaList.add(postMedia);

            PostMediaDto postMediaDto= new PostMediaDto();
            postMediaDto.setPostImageUrl(postMedia.getPostImageUrl());
            postMediaDtosList.add(postMediaDto);
        }

     // add post media list to post and save
        savedPost.getPostImages().clear();
        savedPost.getPostImages().addAll(postMediaList);

        // create post response dto
        PostResponseDto prd= PostUtilMethods.getPostResponseDtoFromPost(user,savedPost,postMediaDtosList);
        postRepository.save(savedPost);

        return prd;
    }




    // delete a single post with id
    public void deletePost(Long id) {

        postRepository.deleteById(id);
    }


    // get a single post with id
    public PostResponseDto getPost(Long id) {

        Post postItem= postRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Post with id " + id + " not found"));

        //create post media dto from post
        List<PostMediaDto> postMediaDtosList= new ArrayList<>();
        postMediaDtosList=createPostMediaDtoListFromPostMediaList(id,postItem);

        //get the authenticated user
        User user= userUtilMethods.getCurrentlyAuthenticatedUser();


        //return the postResponse dto
        return PostUtilMethods.getPostResponseDtoFromPost(user,postItem,postMediaDtosList);

    }

    // converts post media list to post media dto list
    public List<PostMediaDto> createPostMediaDtoListFromPostMediaList(Long id, Post postItem)
    {
        List<PostMediaDto> postMediaDtosList= new ArrayList<>();

        List<PostMedia> postMedias= postItem.getPostImages();
        for(PostMedia media : postMedias)
        {
            PostMediaDto postMediaDto= new PostMediaDto();
            postMediaDto.setPostImageUrl(media.getPostImageUrl());
            postMediaDtosList.add(postMediaDto);
        }
        return postMediaDtosList;
    }

    // converts post media dto list to post media
    public List<PostMedia> createPostMediaListFromPostMediaDtoList(Long id, PostRequestDto newPost,Post oldPost)
    {
        List<PostMedia> postMediaList= new ArrayList<>();

        List<PostMediaDto> postMediaDtos= newPost.getPostImages();
        for(PostMediaDto media : postMediaDtos)
        {
            PostMedia postMedia= new PostMedia();
            postMedia.setPost(oldPost);
            postMedia.setPostImageUrl(media.getPostImageUrl());
            postMediaList.add(postMedia);
        }
        return postMediaList;
    }


    // get all posts with pagination
    public Page<PostResponseDto> getAllPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findAll(pageable);
        // Convert entities to DTOs
        return postsPage.map(this::convertToDto);
    }

    // get posts by category
    public Page<PostResponseDto> getPostsByCategory(String category, Pageable pageable) {
        Page<Post> postsPage = postRepository.findByCategory(category, pageable);
        return postsPage.map(this::convertToDto);
    }


    // converting post to post response dto
    private PostResponseDto convertToDto(Post post) {

        // Map PostMedia to PostMediaDto
        List<PostMediaDto> mediaDtos = post.getPostImages().stream()
                .map(media -> new PostMediaDto(media.getPostImageUrl()))
                .toList();

        // Map User to UserSummeryDto
        User user = post.getUser();
        UserSummeryDto userDto = new UserSummeryDto(
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );

        return new PostResponseDto(
                post.getPostId(),
                post.getTitle(),
                post.getDescription(),
                post.getCategory(),
                post.getExchangeType(),
                post.getStatus(),
                post.getCreatedAt(),
                mediaDtos,
                userDto
        );
    }

    public PostResponseDto updatePost(Long id, PostRequestDto newPost) {

        // get old post with id
        Post oldPost = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post with id " + id + " is not found"
                ));

        // create new media list
        List<PostMedia> postMediaList =
                createPostMediaListFromPostMediaDtoList(id, newPost, oldPost);

        //  modify existing collection instead of replacing it
        oldPost.getPostImages().clear();

        for (PostMedia media : postMediaList) {
            media.setPost(oldPost);
            oldPost.getPostImages().add(media);
        }

        // update other fields
        oldPost.setTitle(newPost.getTitle());
        oldPost.setDescription(newPost.getDescription());
        oldPost.setCategory(newPost.getCategory());
        oldPost.setExchangeType(newPost.getExchangeType());

        Post updatedPost = postRepository.save(oldPost);

        return convertToDto(updatedPost);
    }
}
