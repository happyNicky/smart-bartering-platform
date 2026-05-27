package com.finalyear.liwatch.report;

import com.finalyear.liwatch.report.enums.ReportStatus;
import com.finalyear.liwatch.trust.TrustService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReportAdminService {

    private final UserReportRepository userReportRepository;
    private final TrustService trustService;

    public ReportAdminService(UserReportRepository userReportRepository, TrustService trustService) {
        this.userReportRepository = userReportRepository;
        this.trustService = trustService;
    }

    @Transactional
    public UserReport validateReport(Long reportId) {
        UserReport report = userReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(ReportStatus.VALIDATED);
        report.setValidatedAt(LocalDateTime.now());
        userReportRepository.save(report);
        trustService.recalculate(report.getReportedUser().getId());
        return report;
    }
}
