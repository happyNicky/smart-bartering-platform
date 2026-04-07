package com.finalyear.liwatch.Post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/post")
public class PostController {


    @Autowired
    private PostService postService;

    // create a single post
    @PostMapping("/createPost")
    public ResponseEntity<PostResponseDto> createPost(  @RequestPart("images") List<MultipartFile> images,
                                                             @RequestPart("post") String postJson) throws IOException {
        PostResponseDto createdPost = postService.createPost(images,postJson);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    // delete a single post with id
    @DeleteMapping("/deletePost/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id)
    {
        postService.deletePost(id);
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body("successfully deleted post with id "+id );
    }

    // get a single post with id
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long id)
    {
         PostResponseDto postResponseDto=postService.getPost(id);
        return ResponseEntity.status(HttpStatus.OK).body( postResponseDto );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PostResponseDto> updatePost(@PathVariable Long id,@RequestBody PostRequestDto newPost)
    {
        PostResponseDto postResponseDto=postService.updatePost(id,newPost);
        return ResponseEntity.status(HttpStatus.OK).body(postResponseDto);
    }

    // Get all posts with pagination
    @GetMapping("/allPosts")
    public ResponseEntity<Page<PostResponseDto>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponseDto> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    // Get posts by category with pagination
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<PostResponseDto>> getPostsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponseDto> posts = postService.getPostsByCategory(category, pageable);
        return ResponseEntity.ok(posts);
    }



}
