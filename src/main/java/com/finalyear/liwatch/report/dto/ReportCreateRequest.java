package com.finalyear.liwatch.report.dto;

import com.finalyear.liwatch.report.enums.IssueType;
import com.finalyear.liwatch.report.enums.TargetType;
import lombok.Data;

@Data
public class ReportCreateRequest {
    private TargetType targetType;
    private Long targetId;
    private IssueType issueType;
    private String reason;
    private String evidenceUrl;
}
