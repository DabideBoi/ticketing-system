package com.ticketing.dto;

import com.ticketing.entity.AuditLog;
import com.ticketing.entity.TicketStatus;
import com.ticketing.entity.TicketType;
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
public class ActivityItemResponse {
    private UUID ticketId;
    private String ticketTitle;
    private TicketType ticketType;
    private TicketStatus fromStatus;
    private TicketStatus toStatus;
    private String actionByName;
    private LocalDateTime timestamp;

    public static ActivityItemResponse fromEntity(AuditLog log) {
        return ActivityItemResponse.builder()
                .ticketId(log.getTicket().getId())
                .ticketTitle(log.getTicket().getTitle())
                .ticketType(log.getTicket().getType())
                .fromStatus(log.getFromStatus())
                .toStatus(log.getToStatus())
                .actionByName(log.getActionBy().getFullName())
                .timestamp(log.getTimestamp())
                .build();
    }
}
