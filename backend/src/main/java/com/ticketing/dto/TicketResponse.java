package com.ticketing.dto;

import com.ticketing.entity.Ticket;
import com.ticketing.entity.TicketStatus;
import com.ticketing.entity.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private UUID id;
    private String title;
    private String description;
    private TicketType type;
    private TicketStatus status;
    private UserDto requestor;
    private UserDto approver;
    private UserDto assignee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AuditLogResponse> auditTrail;

    public static TicketResponse fromEntity(Ticket ticket, List<AuditLogResponse> auditTrail) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .type(ticket.getType())
                .status(ticket.getStatus())
                .requestor(ticket.getRequestor() != null ? UserDto.fromEntity(ticket.getRequestor()) : null)
                .approver(ticket.getApprover() != null ? UserDto.fromEntity(ticket.getApprover()) : null)
                .assignee(ticket.getAssignee() != null ? UserDto.fromEntity(ticket.getAssignee()) : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .auditTrail(auditTrail)
                .build();
    }
}
