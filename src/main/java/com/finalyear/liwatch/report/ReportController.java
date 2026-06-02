package com.finalyear.liwatch.report;

import com.finalyear.liwatch.report.dto.ReportCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<UserReport> createReport(@RequestBody ReportCreateRequest request) {
        UserReport report = reportService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }
}
