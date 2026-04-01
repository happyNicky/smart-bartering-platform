package com.finalyear.liwatch.Post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/post")
public class PostController {


    @Autowired
    private PostService postService;

    @PostMapping("/add")
    public Post post(@RequestBody PostRequestDto postItem)
    {
      return postService.createPost(postItem);
    }
    @PostMapping("/delete/{id}")
    public void deletePost(@PathVariable Long id)
    {
        postService.deletePost(id);
    }


}
