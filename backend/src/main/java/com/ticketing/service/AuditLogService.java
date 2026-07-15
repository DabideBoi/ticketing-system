package com.ticketing.service;

import com.ticketing.dto.AuditLogResponse;
import com.ticketing.entity.AuditLog;
import com.ticketing.entity.Ticket;
import com.ticketing.entity.TicketStatus;
import com.ticketing.entity.User;
import com.ticketing.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void record(Ticket ticket, User actionBy, String action, TicketStatus fromStatus, TicketStatus toStatus, String remarks) {
        AuditLog log = AuditLog.builder()
                .ticket(ticket)
                .actionBy(actionBy)
                .action(action)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .remarks(remarks)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> getTrail(Ticket ticket) {
        return auditLogRepository.findByTicketOrderByTimestampAsc(ticket).stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }
}
