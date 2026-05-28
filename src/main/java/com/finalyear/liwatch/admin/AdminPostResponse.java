package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.Post.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminPostResponse {

    private Long postId;
    private String title;
    private String description;
    private String category;
    private String exchangeType;
    private String postType;
    private String status;
    private String location;
    private String lookingFor;
    private LocalDateTime createdAt;
    private boolean isGroupOnly;
    private Long groupId;

    // owner info
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    // flags
    private int reportCount;       // how many times this post has been reported
    private boolean flaggedForReview;

    public static AdminPostResponse from(Post post) {
        return AdminPostResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .description(post.getDescription())
                .category(post.getCategory())
                .exchangeType(post.getExchangeType() != null ? post.getExchangeType().name() : null)
                .postType(post.getPostType() != null ? post.getPostType().name() : null)
                .status(post.getStatus() != null ? post.getStatus().name() : null)
                .location(post.getLocation())
                .lookingFor(post.getLookingFor())
                .createdAt(post.getCreatedAt())
                .isGroupOnly(Boolean.TRUE.equals(post.getIsGroupOnly()))
                .groupId(post.getGroupId())
                .ownerId(post.getUser() != null ? post.getUser().getId() : null)
                .ownerName(post.getUser() != null ? post.getUser().getFullName() : null)
                .ownerEmail(post.getUser() != null ? post.getUser().getEmail() : null)
                .build();
    }
}
