package com.ticketing.dto;

import com.ticketing.entity.AuditLog;
import com.ticketing.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private UUID id;
    private String action;
    private TicketStatus fromStatus;
    private TicketStatus toStatus;
    private String remarks;
    private String actionByName;
    private LocalDateTime timestamp;

    public static AuditLogResponse fromEntity(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .fromStatus(log.getFromStatus())
                .toStatus(log.getToStatus())
                .remarks(log.getRemarks())
                .actionByName(log.getActionBy().getFullName())
                .timestamp(log.getTimestamp())
                .build();
    }
}
