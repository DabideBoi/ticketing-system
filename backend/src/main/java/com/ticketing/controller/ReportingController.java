package com.ticketing.controller;

import com.ticketing.dto.ReportingSummaryResponse;
import com.ticketing.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reporting")
@RequiredArgsConstructor
public class ReportingController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER','ASSIGNER')")
    public ResponseEntity<ReportingSummaryResponse> summary() {
        return ResponseEntity.ok(dashboardService.getReportingSummary());
    }
}
