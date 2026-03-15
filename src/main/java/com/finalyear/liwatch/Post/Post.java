package com.finalyear.liwatch.Post;


import com.finalyear.liwatch.Post.enums.ExchangeType;
import com.finalyear.liwatch.Post.enums.PostType;
import com.finalyear.liwatch.Post.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_type")
    private ExchangeType exchangeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private PostType postType;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


}