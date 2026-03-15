package com.finalyear.liwatch.service;


import com.finalyear.liwatch.service.enumservice.SkillLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "service_duration", nullable = false, length = 50)
    private String serviceDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level")
    private SkillLevel skillLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String availability;


}
