package com.ticketing.service;

import com.ticketing.dto.ApproveRequest;
import com.ticketing.dto.AssignRequest;
import com.ticketing.dto.CreateTicketRequest;
import com.ticketing.dto.StatusUpdateRequest;
import com.ticketing.dto.TicketResponse;
import com.ticketing.entity.Role;
import com.ticketing.entity.Ticket;
import com.ticketing.entity.TicketStatus;
import com.ticketing.entity.TicketType;
import com.ticketing.entity.User;
import com.ticketing.exception.ApiException;
import com.ticketing.repository.TicketRepository;
import com.ticketing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, User requestor) {
        TicketStatus initialStatus = request.getType() == TicketType.DB_FIX
                ? TicketStatus.FOR_APPROVAL
                : TicketStatus.FOR_ASSIGNMENT;

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .status(initialStatus)
                .requestor(requestor)
                .build();
        ticket = ticketRepository.save(ticket);

        auditLogService.record(ticket, requestor, "STATUS_CHANGE", null, initialStatus, null);

        if (initialStatus == TicketStatus.FOR_APPROVAL) {
            notificationService.notifyDbFixCreated(ticket);
        } else {
            notificationService.notifyAssignmentRequired(ticket, requestor);
        }

        return toResponse(ticket, false);
    }

    public List<TicketResponse> getTicketsForUser(User user) {
        List<Ticket> tickets = switch (user.getRole()) {
            case REQUESTOR -> ticketRepository.findByRequestor(user);
            case APPROVER -> ticketRepository.findByStatus(TicketStatus.FOR_APPROVAL);
            case ASSIGNER -> ticketRepository.findByStatus(TicketStatus.FOR_ASSIGNMENT);
            case ASSIGNEE -> ticketRepository.findByAssigneeAndStatusIn(
                    user, List.of(TicketStatus.ASSIGNED, TicketStatus.ONGOING));
            case ADMIN -> ticketRepository.findAll();
        };
        return tickets.stream().map(t -> toResponse(t, false)).toList();
    }

    public TicketResponse getTicketById(UUID id) {
        Ticket ticket = getTicketOrThrow(id);
        return toResponse(ticket, true);
    }

    @Transactional
    public TicketResponse approve(UUID id, ApproveRequest request, User approver) {
        Ticket ticket = getTicketOrThrow(id);
        requireStatus(ticket, TicketStatus.FOR_APPROVAL);

        TicketStatus from = ticket.getStatus();
        ticket.setApprover(approver);
        ticket.setStatus(Boolean.TRUE.equals(request.getApproved()) ? TicketStatus.FOR_ASSIGNMENT : TicketStatus.CLOSE);
        ticket = ticketRepository.save(ticket);

        auditLogService.record(ticket, approver, "APPROVAL", from, ticket.getStatus(), request.getRemarks());

        if (ticket.getStatus() == TicketStatus.FOR_ASSIGNMENT) {
            notificationService.notifyAssignmentRequired(ticket, approver);
        }

        return toResponse(ticket, false);
    }

    @Transactional
    public TicketResponse assign(UUID id, AssignRequest request, User assigner) {
        Ticket ticket = getTicketOrThrow(id);
        requireStatus(ticket, TicketStatus.FOR_ASSIGNMENT);

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Assignee not found"));
        if (assignee.getRole() != Role.ASSIGNEE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Selected user does not have the Assignee role");
        }

        TicketStatus from = ticket.getStatus();
        ticket.setAssignee(assignee);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket = ticketRepository.save(ticket);

        auditLogService.record(ticket, assigner, "ASSIGNMENT", from, TicketStatus.ASSIGNED, null);
        notificationService.notifyAssigned(ticket);

        return toResponse(ticket, false);
    }

    @Transactional
    public TicketResponse updateStatus(UUID id, StatusUpdateRequest request, User actor) {
        Ticket ticket = getTicketOrThrow(id);
        TicketStatus target = request.getStatus();
        TicketStatus from = ticket.getStatus();

        validateTransition(ticket, target, actor);

        ticket.setStatus(target);
        ticket = ticketRepository.save(ticket);

        auditLogService.record(ticket, actor, "STATUS_CHANGE", from, target, null);

        if (target == TicketStatus.FOR_CLOSE) {
            notificationService.notifyForClose(ticket);
        } else if (target == TicketStatus.CLOSE) {
            notificationService.notifyClosed(ticket);
        }

        return toResponse(ticket, false);
    }

    private void validateTransition(Ticket ticket, TicketStatus target, User actor) {
        Role role = actor.getRole();

        switch (target) {
            case ONGOING -> {
                requireStatus(ticket, TicketStatus.ASSIGNED);
                requireRole(role, EnumSet.of(Role.ASSIGNEE, Role.ADMIN));
            }
            case FOR_CLOSE -> {
                requireStatus(ticket, TicketStatus.ONGOING);
                requireRole(role, EnumSet.of(Role.ASSIGNEE, Role.ADMIN));
            }
            case CLOSE -> {
                requireStatus(ticket, TicketStatus.FOR_CLOSE);
                requireRole(role, EnumSet.of(Role.REQUESTOR, Role.ASSIGNER, Role.ADMIN));
            }
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported status transition target: " + target);
        }
    }

    private void requireStatus(Ticket ticket, TicketStatus expected) {
        if (ticket.getStatus() != expected) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Ticket is in status " + ticket.getStatus() + ", expected " + expected);
        }
    }

    private void requireRole(Role role, Set<Role> allowed) {
        if (!allowed.contains(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Role " + role + " cannot perform this transition");
        }
    }

    private Ticket getTicketOrThrow(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ticket not found: " + id));
    }

    private TicketResponse toResponse(Ticket ticket, boolean includeAuditTrail) {
        return TicketResponse.fromEntity(ticket, includeAuditTrail ? auditLogService.getTrail(ticket) : List.of());
    }
}
