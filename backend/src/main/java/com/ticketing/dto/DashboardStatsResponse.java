package com.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long myOpenTickets;
    private long pendingApprovals;
    private long forAssignment;
    private long assignedToMe;
    private long ongoing;
}
