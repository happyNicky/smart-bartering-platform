package com.finalyear.liwatch.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@AdminOnly
public class AdminActionLogController {

    private final AdminActionLogRepository logRepository;

    @GetMapping
    public ResponseEntity<List<AdminActionLog>> getAllLogs() {
        return ResponseEntity.ok(logRepository.findAllByOrderByActionTimeDesc());
    }
}
