package com.finalyear.liwatch.service;


import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.service.enumservice.SkillLevel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("service")
public class Service extends Post {

    @Column(name = "service_duration", nullable = false, length = 50)
    private String serviceDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level")
    private SkillLevel skillLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String availability;


}
