package com.finalyear.liwatch.rating;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.rating.enums.RatingWindowStatus;
import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rating_windows")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "window_id")
    private Long windowId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barter_id", nullable = false, unique = true)
    private Barter barter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(name = "user1_submitted", nullable = false)
    private Boolean user1Submitted = false;

    @Column(name = "user2_submitted", nullable = false)
    private Boolean user2Submitted = false;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RatingWindowStatus status = RatingWindowStatus.Open;
}
