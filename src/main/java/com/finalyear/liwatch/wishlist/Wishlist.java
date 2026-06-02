package com.finalyear.liwatch.wishlist;

import com.finalyear.liwatch.Post.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlists", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long wishlistId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}