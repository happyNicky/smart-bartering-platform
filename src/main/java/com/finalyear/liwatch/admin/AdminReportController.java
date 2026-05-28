package com.finalyear.liwatch.admin;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for report management.
 *
 * Base path: /api/admin/reports
 *
 * Report lifecycle:
 *   PENDING  ──(validate)──▶  RESOLVED  (with optional WARN or SUSPEND on user)
 *   PENDING  ──(dismiss) ──▶  DISMISSED
 *   RESOLVED │
 *   DISMISSED├──(reopen) ──▶  PENDING
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@AdminOnly
public class AdminReportController {

    private final AdminReportService reportService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/reports
    //   ?status=PENDING&keyword=john&page=0&size=20
    //
    // Tip: use status=PENDING as your default moderation queue view.
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<AdminApiResponse<List<AdminReportResponse>>> listReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(reportService.listReports(status, keyword, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/reports/against/{userId}
    // All reports filed AGAINST a specific user
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/against/{userId}")
    public ResponseEntity<AdminApiResponse<List<AdminReportResponse>>> listReportsAgainstUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                reportService.listReportsByReportedUser(userId, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/reports/by/{userId}
    // All reports filed BY a specific user
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/by/{userId}")
    public ResponseEntity<AdminApiResponse<List<AdminReportResponse>>> listReportsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                reportService.listReportsByReporter(userId, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/reports/{reportId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{reportId}")
    public ResponseEntity<AdminApiResponse<AdminReportResponse>> getReportDetail(
            @PathVariable Long reportId
    ) {
        return ResponseEntity.ok(reportService.getReportDetail(reportId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/reports/{reportId}/validate
    //
    // Report is legitimate. Marks it RESOLVED and optionally acts on the
    // reported user in the same call.
    //
    // Body example — just resolve:
    //   { "reason": "Confirmed scam", "userAction": "NONE" }
    //
    // Body example — resolve and suspend:
    //   { "reason": "Repeated violations", "userAction": "SUSPEND" }
    //
    // Body example — resolve with warning:
    //   { "reason": "First offence", "userAction": "WARN" }
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{reportId}/validate")
    public ResponseEntity<AdminApiResponse<AdminReportResponse>> validateReport(
            @PathVariable Long reportId,
            @Valid @RequestBody AdminReportActionRequest req
    ) {
        return ResponseEntity.ok(reportService.validateReport(reportId, req));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/reports/{reportId}/dismiss
    // Report is invalid / false — marks it DISMISSED, no action on user.
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{reportId}/dismiss")
    public ResponseEntity<AdminApiResponse<AdminReportResponse>> dismissReport(
            @PathVariable Long reportId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(reportService.dismissReport(reportId, req));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/reports/{reportId}/reopen
    // Re-opens a resolved or dismissed report back to PENDING for re-review.
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{reportId}/reopen")
    public ResponseEntity<AdminApiResponse<AdminReportResponse>> reopenReport(
            @PathVariable Long reportId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(reportService.reopenReport(reportId, req));
    }
}
