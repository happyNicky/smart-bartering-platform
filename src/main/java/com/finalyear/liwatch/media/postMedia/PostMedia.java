package com.finalyear.liwatch.media.postMedia;

import com.finalyear.liwatch.Post.Post;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "postMedia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMedia {


    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    @Id
    private Long id;

    @Column(name = "post_image", nullable = false)
    private String postImageUrl;

    @ManyToOne
    @JoinColumn(name = "post",nullable = false)
    private Post post;
}