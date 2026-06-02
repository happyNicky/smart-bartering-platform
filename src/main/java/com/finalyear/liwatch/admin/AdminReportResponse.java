package com.finalyear.liwatch.admin;


import com.finalyear.liwatch.report.UserReport;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminReportResponse {

    private Long reportId;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime validatedAt;

    private String targetType;
    private String issueType;
    private String evidenceUrl;

    private Long reportedPostId;
    private Long reportedBarterId;

    // reported user
    private Long reportedUserId;
    private String reportedUserName;
    private String reportedUserEmail;

    // reporter
    private Long reporterUserId;
    private String reporterUserName;
    private String reporterUserEmail;

    public static AdminReportResponse from(UserReport report) {
        return AdminReportResponse.builder()
                .reportId(report.getReportId())
                .status(report.getStatus().name())
                .reason(report.getReason())
                .createdAt(report.getCreatedAt())
                .validatedAt(report.getValidatedAt())
                .targetType(report.getTargetType() != null ? report.getTargetType().name() : null)
                .issueType(report.getIssueType() != null ? report.getIssueType().name() : null)
                .evidenceUrl(report.getEvidenceUrl())
                .reportedPostId(report.getReportedPost() != null ? report.getReportedPost().getPostId() : null)
                .reportedBarterId(report.getReportedBarter() != null ? report.getReportedBarter().getId() : null)
                .reportedUserId(report.getReportedUser() != null ? report.getReportedUser().getId() : null)
                .reportedUserName(report.getReportedUser() != null ? report.getReportedUser().getFullName() : null)
                .reportedUserEmail(report.getReportedUser() != null ? report.getReportedUser().getEmail() : null)
                .reporterUserId(report.getReporterUser() != null ? report.getReporterUser().getId() : null)
                .reporterUserName(report.getReporterUser() != null ? report.getReporterUser().getFullName() : null)
                .reporterUserEmail(report.getReporterUser() != null ? report.getReporterUser().getEmail() : null)
                .build();
    }
}
