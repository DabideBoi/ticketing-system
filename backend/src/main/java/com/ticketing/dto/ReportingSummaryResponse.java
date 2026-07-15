package com.ticketing.dto;

import com.ticketing.entity.TicketStatus;
import com.ticketing.entity.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportingSummaryResponse {
    private Map<TicketStatus, Long> statusCounts;
    private Map<TicketType, Long> typeCounts;
    private long totalTickets;
}
