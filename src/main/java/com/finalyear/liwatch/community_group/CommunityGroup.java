package com.finalyear.liwatch.community_group;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", nullable = false, length = 150)
    private String groupName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = true, length = 100)
    private String location;

    @Column(nullable = true, length = 100)
    private String category;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "is_private", nullable = false)
    @Builder.Default
    private Boolean isPrivate = false;

    @Column(name = "group_category", nullable = true)
    private String groupCategory;

    @Column(name = "item_subcategory", nullable = true)
    private String itemSubcategory;

    @Column(name = "cover_image_url", nullable = true, length = 500)
    private String coverImageUrl;

    @Transient
    private Long memberCount;

    @Transient
    private Double groupAverageRating;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ACTIVE,
        SUSPENDED
    }
}
