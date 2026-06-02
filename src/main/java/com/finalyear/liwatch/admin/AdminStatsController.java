package com.finalyear.liwatch.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@AdminOnly
public class AdminStatsController {

    private final AdminStatsService statsService;

    @GetMapping
    public ResponseEntity<AdminApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }
}
