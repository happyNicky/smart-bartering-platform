package com.finalyear.liwatch.Post.post_management;

import com.finalyear.liwatch.Post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

<<<<<<<< HEAD:src/main/java/com/finalyear/liwatch/test_repositories/TestPostRepository.java
public interface TestPostRepository extends JpaRepository<Post,Long> {
========
@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
>>>>>>>> 97f56fe (Added  Barter repository,service,and controller to create darter and also added DigitalAgreement repository,service and controller to enable functionality of creating partial and full digital agreements):src/main/java/com/finalyear/liwatch/Post/post_management/PostRepository.java
}
