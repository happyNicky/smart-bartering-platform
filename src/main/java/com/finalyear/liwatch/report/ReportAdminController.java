package com.finalyear.liwatch.report;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportAdminController {

    private final ReportAdminService reportAdminService;

    public ReportAdminController(ReportAdminService reportAdminService) {
        this.reportAdminService = reportAdminService;
    }

    /**
     * Admin validates a report. Immediately triggers trust score recalculation
     * for the reported user. Requires ADMIN role.
     */
    @PostMapping("/{reportId}/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserReport> validateReport(@PathVariable Long reportId) {
        return ResponseEntity.ok(reportAdminService.validateReport(reportId));
    }
}
