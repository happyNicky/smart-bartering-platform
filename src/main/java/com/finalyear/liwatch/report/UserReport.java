package com.finalyear.liwatch.report;

import com.finalyear.liwatch.report.enums.ReportStatus;
import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser; // Made nullable for POST/BARTER reports

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_post_id")
    private com.finalyear.liwatch.Post.Post reportedPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_barter_id")
    private com.finalyear.liwatch.barter.Barter reportedBarter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_cycle_barter_id")
    private com.finalyear.liwatch.cycleswap.model.CycleBarter reportedCycleBarter;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private com.finalyear.liwatch.report.enums.TargetType targetType = com.finalyear.liwatch.report.enums.TargetType.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type")
    private com.finalyear.liwatch.report.enums.IssueType issueType;

    @Column(name = "evidence_url")
    private String evidenceUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporterUser;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;
}
