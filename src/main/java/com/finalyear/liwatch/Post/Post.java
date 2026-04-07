package com.finalyear.liwatch.Post;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.finalyear.liwatch.Item.Item;
import com.finalyear.liwatch.Post.enums.ExchangeType;
import com.finalyear.liwatch.Post.enums.PostType;
import com.finalyear.liwatch.Post.enums.Status;
import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.media.postMedia.PostMedia;
import com.finalyear.liwatch.service.Service;
import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)

public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "post_id")
    protected Long postId;

    @Column(nullable = false, length = 240)
    protected String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    protected String description;

    @Column(nullable = false, length = 100)
    protected String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_type")
    protected ExchangeType exchangeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type",nullable = false)
    protected PostType postType;

    @Enumerated(EnumType.STRING)
    protected Status status;

    @Column(name = "location")
    protected String location;

    @Column(name = "looking_for")
    protected String lookingFor;

    @Column(name = "created_at", nullable = false)
    protected LocalDateTime createdAt=LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id")
    protected User user;

    @OneToMany(mappedBy = "offeredPost", fetch = FetchType.LAZY)
    protected List<DirectSwapRequest> offeredRequests;

    @OneToMany(mappedBy = "requestedPost", fetch = FetchType.LAZY)
    protected List<DirectSwapRequest> requestedInRequests;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval= true ,fetch = FetchType.LAZY)
    private List<PostMedia> postImages= new ArrayList<>();


}