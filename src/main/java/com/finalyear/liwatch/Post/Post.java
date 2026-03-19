package com.finalyear.liwatch.Post;


import com.finalyear.liwatch.Post.enums.ExchangeType;
import com.finalyear.liwatch.Post.enums.Status;
import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


import java.util.List;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "post_type")
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
    protected Status status;

    @Column(name = "created_at", nullable = false)
    protected LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "user_id")
    protected User user;

    // used  offered in direct request
    @OneToMany(mappedBy = "offeredPost")
    protected List<DirectSwapRequest> offeredRequests;

    //used as requested
    @OneToMany(mappedBy = "requestedPost")
    protected List<DirectSwapRequest> requestedInRequests;


}