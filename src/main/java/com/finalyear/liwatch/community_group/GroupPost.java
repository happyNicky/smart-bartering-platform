package com.finalyear.liwatch.community_group;

import com.finalyear.liwatch.Post.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_posts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "post_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_post_id")
    private Long groupPostId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CommunityGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @Column(name = "shared_at", nullable = false)
    private LocalDateTime sharedAt;
}
