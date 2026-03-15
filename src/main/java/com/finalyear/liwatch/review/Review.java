package com.finalyear.liwatch.review;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "rating_id", nullable = false)
    private Long ratingId;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_published")
    private Boolean isPublished = false;
}