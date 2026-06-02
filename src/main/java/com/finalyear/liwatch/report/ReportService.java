package com.finalyear.liwatch.report;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.PostRepository;
import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.barter.barter_managment.BarterRepository;
import com.finalyear.liwatch.report.dto.ReportCreateRequest;
import com.finalyear.liwatch.report.enums.ReportStatus;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import com.finalyear.liwatch.cycleswap.model.CycleBarter;
import com.finalyear.liwatch.cycleswap.repository.CycleBarterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BarterRepository barterRepository;
    private final CycleBarterRepository cycleBarterRepository;
    private final UserUtilService userUtilService;

    public UserReport createReport(ReportCreateRequest request) {
        User reporter = userUtilService.getCurrentlyAuthenticatedUser();

        UserReport report = UserReport.builder()
                .reporterUser(reporter)
                .targetType(request.getTargetType())
                .issueType(request.getIssueType())
                .reason(request.getReason())
                .evidenceUrl(request.getEvidenceUrl())
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        switch (request.getTargetType()) {
            case USER:
                User reportedUser = userRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                report.setReportedUser(reportedUser);
                break;
            case POST:
                Post reportedPost = postRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
                report.setReportedPost(reportedPost);
                // Also optionally link the post's author as the reported user
                report.setReportedUser(reportedPost.getUser());
                
                // Automatically flag the post so it appears in the admin's Flagged Queue
                reportedPost.setStatus(com.finalyear.liwatch.Post.enums.Status.FLAGGED);
                postRepository.save(reportedPost);
                break;
            case BARTER:
                Barter reportedBarter = barterRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Barter not found"));
                report.setReportedBarter(reportedBarter);
                // The reported user is the other party in the barter
                if (reportedBarter.getUserA().getId().equals(reporter.getId())) {
                    report.setReportedUser(reportedBarter.getUserB());
                } else if (reportedBarter.getUserB().getId().equals(reporter.getId())) {
                    report.setReportedUser(reportedBarter.getUserA());
                }
                break;
            case CYCLE_BARTER:
                CycleBarter reportedCycleBarter = cycleBarterRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cycle Barter not found"));
                report.setReportedCycleBarter(reportedCycleBarter);
                // Leave reportedUser null as cycle involves 3 users, admin will investigate the cycle.
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid target type");
        }

        return reportRepository.save(report);
    }
}
