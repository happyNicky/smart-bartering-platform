package com.finalyear.liwatch.media;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long mediaId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "service_duration", nullable = false, length = 50)
    private String serviceDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level")
    private SkillLevel skillLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String availability;

    public enum SkillLevel {
        BEGINNER,
        EXPERT
    }
}