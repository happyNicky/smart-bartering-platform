package com.finalyear.liwatch.test_repositories;

import com.finalyear.liwatch.Post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long> {
}
