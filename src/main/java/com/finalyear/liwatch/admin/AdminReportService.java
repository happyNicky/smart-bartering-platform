package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.report.UserReport;

import com.finalyear.liwatch.report.enums.ReportStatus;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.enums.Role;
import com.finalyear.liwatch.userManagement.utils.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final AdminReportRepository reportRepo;
    private final AdminUserRepository   userRepo;

    // ── List / search reports ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<List<AdminReportResponse>> listReports(
            String status,
            String keyword,
            Pageable pageable) {

        Page<AdminReportResponse> mapped = reportRepo
                .searchReports(status, keyword, pageable)
                .map(report -> AdminReportResponse.from(report));

        return AdminApiResponse.ofPage(mapped);
    }

    // ── Reports filed against a user ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<List<AdminReportResponse>> listReportsByReportedUser(
            Long userId, Pageable pageable) {

        Page<AdminReportResponse> mapped = reportRepo
                .findByReportedUserId(userId, pageable)
                .map(report -> AdminReportResponse.from(report));

        return AdminApiResponse.ofPage(mapped);
    }

    // ── Reports filed by a user ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<List<AdminReportResponse>> listReportsByReporter(
            Long userId, Pageable pageable) {

        Page<AdminReportResponse> mapped = reportRepo
                .findByReporterUserId(userId, pageable)
                .map(report -> AdminReportResponse.from(report));

        return AdminApiResponse.ofPage(mapped);
    }

    // ── Single report detail ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<AdminReportResponse> getReportDetail(Long reportId) {
        UserReport report = findOrThrow(reportId);
        return AdminApiResponse.ok(AdminReportResponse.from(report));
    }

    // ── Validate report (PENDING → RESOLVED + optional user action) ───────────

    /**
     * Admin reviews the report and decides it is legitimate.
     *
     * The userAction field controls what happens to the reported user:
     *   NONE    → just close the report
     *   WARN    → log a warning (currently just logged; extend to notifications later)
     *   SUSPEND → immediately suspend the reported user
     */
    @Transactional
    public AdminApiResponse<AdminReportResponse> validateReport(
            Long reportId, AdminReportActionRequest req) {

        UserReport report = findOrThrow(reportId);
        assertPending(report);

        report.setStatus(ReportStatus.VALIDATED);
        report.setValidatedAt(LocalDateTime.now());
        reportRepo.save(report);

        // act on the reported user based on the chosen action
        User reportedUser = report.getReportedUser();
        switch (req.getUserAction()) {

            case SUSPEND -> {
                if (reportedUser.getRole() == Role.ADMIN) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Cannot suspend an admin account via report validation.");
                }
                reportedUser.setStatus(Status.SUSPENDED);
                reportedUser.setEnabled(false);
                userRepo.save(reportedUser);
                log.warn("[ADMIN] Validated report {} → SUSPENDED user {} — reason: {}",
                        reportId, reportedUser.getId(), req.getReason());
            }

            case WARN -> {
                // placeholder: in a full system, push a warning notification to the user
                log.warn("[ADMIN] Validated report {} → WARNED user {} — reason: {}",
                        reportId, reportedUser.getId(), req.getReason());
            }

            default -> {
                log.info("[ADMIN] Validated report {} → no user action — reason: {}",
                        reportId, req.getReason());
            }
        }

        return AdminApiResponse.ok(
                AdminReportResponse.from(report),
                buildValidationMessage(req.getUserAction(), reportedUser.getFullName()));
    }

    // ── Dismiss report (PENDING → DISMISSED, no action on user) ──────────────

    @Transactional
    public AdminApiResponse<AdminReportResponse> dismissReport(
            Long reportId, AdminActionRequest req) {

        UserReport report = findOrThrow(reportId);
        assertPending(report);

        report.setStatus(ReportStatus.REJECTED);
        report.setValidatedAt(LocalDateTime.now());
        reportRepo.save(report);

        log.info("[ADMIN] Dismissed report {} — reason: {}", reportId, req.getReason());
        return AdminApiResponse.ok(
                AdminReportResponse.from(report),
                "Report dismissed.");
    }

    // ── Re-open a rejected/validated report (→ PENDING) ──────────────────────

    @Transactional
    public AdminApiResponse<AdminReportResponse> reopenReport(
            Long reportId, AdminActionRequest req) {

        UserReport report = findOrThrow(reportId);

        if (report.getStatus() == ReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Report is already pending.");
        }

        report.setStatus(ReportStatus.PENDING);
        report.setValidatedAt(null);
        reportRepo.save(report);

        log.info("[ADMIN] Re-opened report {} — reason: {}", reportId, req.getReason());
        return AdminApiResponse.ok(
                AdminReportResponse.from(report),
                "Report re-opened and set back to pending.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UserReport findOrThrow(Long reportId) {
        return reportRepo.findByIdWithUsers(reportId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Report not found: id=" + reportId));
    }

    private void assertPending(UserReport report) {
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Report is already " + report.getStatus().name().toLowerCase()
                            + ". Re-open it first if you need to change the outcome.");
        }
    }

    private String buildValidationMessage(
            AdminReportActionRequest.UserAction action, String userName) {
        return switch (action) {
            case SUSPEND -> "Report validated. " + userName + " has been suspended.";
            case WARN    -> "Report validated. Warning logged against " + userName + ".";
            default      -> "Report validated. No action taken on user.";
        };
    }
}
