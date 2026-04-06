package com.finalyear.liwatch.Post.post_management;

import com.finalyear.liwatch.Post.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    @Autowired
    private PostRepository repository;
    public Post getPost(Long id){
        return repository.findById(id).orElseThrow(
                ()-> new RuntimeException("Post not found!")
        );
    }
}
