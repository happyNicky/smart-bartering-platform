package com.finalyear.liwatch.rating;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"barter_id", "from_user_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long ratingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barter_id")
    private Barter barter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_barter_id")
    private com.finalyear.liwatch.cycleswap.model.CycleBarter cycleBarter;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
