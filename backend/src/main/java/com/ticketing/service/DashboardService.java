package com.ticketing.service;

import com.ticketing.dto.DashboardStatsResponse;
import com.ticketing.dto.ReportingSummaryResponse;
import com.ticketing.entity.TicketStatus;
import com.ticketing.entity.TicketType;
import com.ticketing.entity.User;
import com.ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardStatsResponse getStats(User user) {
        DashboardStatsResponse.DashboardStatsResponseBuilder stats = DashboardStatsResponse.builder();

        switch (user.getRole()) {
            case REQUESTOR -> stats.myOpenTickets(ticketRepository.countByRequestorAndStatusNot(user, TicketStatus.CLOSE));
            case APPROVER -> stats.pendingApprovals(ticketRepository.countByStatus(TicketStatus.FOR_APPROVAL));
            case ASSIGNER -> stats.forAssignment(ticketRepository.countByStatus(TicketStatus.FOR_ASSIGNMENT));
            case ASSIGNEE -> stats
                    .assignedToMe(ticketRepository.countByAssigneeAndStatus(user, TicketStatus.ASSIGNED))
                    .ongoing(ticketRepository.countByAssigneeAndStatus(user, TicketStatus.ONGOING));
            case ADMIN -> stats
                    .myOpenTickets(ticketRepository.countByRequestorAndStatusNot(user, TicketStatus.CLOSE))
                    .pendingApprovals(ticketRepository.countByStatus(TicketStatus.FOR_APPROVAL))
                    .forAssignment(ticketRepository.countByStatus(TicketStatus.FOR_ASSIGNMENT))
                    .ongoing(ticketRepository.countByStatus(TicketStatus.ONGOING));
        }

        return stats.build();
    }

    public ReportingSummaryResponse getReportingSummary() {
        Map<TicketStatus, Long> statusCounts = new EnumMap<>(TicketStatus.class);
        for (TicketStatus status : TicketStatus.values()) {
            statusCounts.put(status, ticketRepository.countByStatus(status));
        }

        Map<TicketType, Long> typeCounts = new EnumMap<>(TicketType.class);
        for (TicketType type : TicketType.values()) {
            typeCounts.put(type, ticketRepository.countByType(type));
        }

        return ReportingSummaryResponse.builder()
                .statusCounts(statusCounts)
                .typeCounts(typeCounts)
                .totalTickets(ticketRepository.count())
                .build();
    }
}
