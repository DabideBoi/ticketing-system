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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private NotificationService notificationService;

    private TicketService ticketService;

    private User requestor;
    private User approver;
    private User assigner;
    private User assignee;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(ticketRepository, userRepository, auditLogService, notificationService);

        requestor = User.builder().id(UUID.randomUUID()).email("r@x.com").role(Role.REQUESTOR).fullName("Req").build();
        approver = User.builder().id(UUID.randomUUID()).email("a@x.com").role(Role.APPROVER).fullName("App").build();
        assigner = User.builder().id(UUID.randomUUID()).email("s@x.com").role(Role.ASSIGNER).fullName("Asr").build();
        assignee = User.builder().id(UUID.randomUUID()).email("e@x.com").role(Role.ASSIGNEE).fullName("Ase").build();

        lenient().when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createDbFixTicket_landsInForApproval_andNotifiesApprovers() {
        CreateTicketRequest request = new CreateTicketRequest("Fix", "desc", TicketType.DB_FIX);

        TicketResponse response = ticketService.createTicket(request, requestor);

        assertThat(response.getStatus()).isEqualTo(TicketStatus.FOR_APPROVAL);
        verify(notificationService).notifyDbFixCreated(any(Ticket.class));
        verify(notificationService, never()).notifyAssignmentRequired(any(), any());
        verify(auditLogService).record(any(Ticket.class), eq(requestor), eq("STATUS_CHANGE"), eq(null), eq(TicketStatus.FOR_APPROVAL), eq(null));
    }

    @Test
    void createNonDbFixTicket_landsInForAssignment_andNotifiesAssigners() {
        CreateTicketRequest request = new CreateTicketRequest("Svc", "desc", TicketType.SERVICE_REQUEST);

        TicketResponse response = ticketService.createTicket(request, requestor);

        assertThat(response.getStatus()).isEqualTo(TicketStatus.FOR_ASSIGNMENT);
        verify(notificationService).notifyAssignmentRequired(any(Ticket.class), eq(requestor));
        verify(notificationService, never()).notifyDbFixCreated(any());
    }

    @Test
    void approve_true_movesToForAssignment_andNotifiesAssigners() {
        Ticket ticket = dbFixTicket(TicketStatus.FOR_APPROVAL);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        TicketResponse response = ticketService.approve(ticket.getId(), new ApproveRequest(true, "ok"), approver);

        assertThat(response.getStatus()).isEqualTo(TicketStatus.FOR_ASSIGNMENT);
        verify(notificationService).notifyAssignmentRequired(any(Ticket.class), eq(approver));
    }

    @Test
    void approve_false_movesToClose_andDoesNotNotifyAssigners() {
        Ticket ticket = dbFixTicket(TicketStatus.FOR_APPROVAL);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        TicketResponse response = ticketService.approve(ticket.getId(), new ApproveRequest(false, "nope"), approver);

        assertThat(response.getStatus()).isEqualTo(TicketStatus.CLOSE);
        verify(notificationService, never()).notifyAssignmentRequired(any(), any());
    }

    @Test
    void approve_wrongSourceStatus_throwsConflict() {
        Ticket ticket = dbFixTicket(TicketStatus.ASSIGNED);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.approve(ticket.getId(), new ApproveRequest(true, null), approver))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void assign_movesToAssigned_andNotifiesAssignee() {
        Ticket ticket = dbFixTicket(TicketStatus.FOR_ASSIGNMENT);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.of(assignee));

        TicketResponse response = ticketService.assign(ticket.getId(), new AssignRequest(assignee.getId()), assigner);

        assertThat(response.getStatus()).isEqualTo(TicketStatus.ASSIGNED);
        assertThat(response.getAssignee().getId()).isEqualTo(assignee.getId());
        verify(notificationService).notifyAssigned(any(Ticket.class));
    }

    @Test
    void assign_toNonAssigneeRole_throwsBadRequest() {
        Ticket ticket = dbFixTicket(TicketStatus.FOR_ASSIGNMENT);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(userRepository.findById(approver.getId())).thenReturn(Optional.of(approver));

        assertThatThrownBy(() -> ticketService.assign(ticket.getId(), new AssignRequest(approver.getId()), assigner))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void updateStatus_ongoing_requiresAssigneeOrAdmin() {
        Ticket ticket = dbFixTicket(TicketStatus.ASSIGNED);
        ticket.setAssignee(assignee);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.updateStatus(ticket.getId(), new StatusUpdateRequest(TicketStatus.ONGOING), requestor))
                .isInstanceOf(ApiException.class);

        TicketResponse response = ticketService.updateStatus(ticket.getId(), new StatusUpdateRequest(TicketStatus.ONGOING), assignee);
        assertThat(response.getStatus()).isEqualTo(TicketStatus.ONGOING);
    }

    @Test
    void updateStatus_close_requiresRequestorAssignerOrAdmin_andNotifiesBoth() {
        Ticket ticket = dbFixTicket(TicketStatus.FOR_CLOSE);
        ticket.setAssignee(assignee);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.updateStatus(ticket.getId(), new StatusUpdateRequest(TicketStatus.CLOSE), assignee))
                .isInstanceOf(ApiException.class);

        TicketResponse response = ticketService.updateStatus(ticket.getId(), new StatusUpdateRequest(TicketStatus.CLOSE), requestor);
        assertThat(response.getStatus()).isEqualTo(TicketStatus.CLOSE);
        verify(notificationService).notifyClosed(any(Ticket.class));
    }

    @Test
    void everyTransition_writesExactlyOneAuditRow() {
        Ticket ticket = dbFixTicket(TicketStatus.FOR_APPROVAL);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        ticketService.approve(ticket.getId(), new ApproveRequest(true, "ok"), approver);

        verify(auditLogService, times(1)).record(any(Ticket.class), eq(approver), eq("APPROVAL"),
                eq(TicketStatus.FOR_APPROVAL), eq(TicketStatus.FOR_ASSIGNMENT), eq("ok"));
    }

    private Ticket dbFixTicket(TicketStatus status) {
        return Ticket.builder()
                .id(UUID.randomUUID())
                .title("t")
                .description("d")
                .type(TicketType.DB_FIX)
                .status(status)
                .requestor(requestor)
                .build();
    }
}
