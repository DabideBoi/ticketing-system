package com.ticketing.controller;

import com.ticketing.dto.ActivityItemResponse;
import com.ticketing.dto.DashboardStatsResponse;
import com.ticketing.security.UserPrincipal;
import com.ticketing.service.AuditLogService;
import com.ticketing.service.DashboardService;
import com.ticketing.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final TicketService ticketService;
    private final AuditLogService auditLogService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> stats(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getStats(principal.getUser()));
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityItemResponse>> activity(@AuthenticationPrincipal UserPrincipal principal) {
        var scopedTickets = ticketService.getScopedTickets(principal.getUser());
        return ResponseEntity.ok(auditLogService.getRecentActivity(scopedTickets));
    }
}
