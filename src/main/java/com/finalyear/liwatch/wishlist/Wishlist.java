package com.finalyear.liwatch.wishlist;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlists")
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

    @Column(name = "desired_item", nullable = false, length = 150)
    private String desiredItem;

    @Column(name = "alert_enabled")
    private Boolean alertEnabled = true;
}