package com.finalyear.liwatch.report;

import com.finalyear.liwatch.report.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {

    long countByReportedUser_IdAndStatus(Long reportedUserId, ReportStatus status);
}
