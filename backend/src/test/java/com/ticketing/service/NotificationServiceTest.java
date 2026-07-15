package com.ticketing.service;

import com.ticketing.entity.Role;
import com.ticketing.entity.Ticket;
import com.ticketing.entity.TicketStatus;
import com.ticketing.entity.TicketType;
import com.ticketing.entity.User;
import com.ticketing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;

    private User requestor;
    private User assignee;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(restTemplate, userRepository);
        ReflectionTestUtils.setField(notificationService, "webhookUrl", "https://example.com/webhook");

        requestor = User.builder().id(UUID.randomUUID()).email("r@x.com").role(Role.REQUESTOR).fullName("Req").build();
        assignee = User.builder().id(UUID.randomUUID()).email("e@x.com").role(Role.ASSIGNEE).fullName("Ase").build();
        ticket = Ticket.builder()
                .id(UUID.randomUUID())
                .title("t").description("d")
                .type(TicketType.DB_FIX)
                .status(TicketStatus.FOR_APPROVAL)
                .requestor(requestor)
                .assignee(assignee)
                .build();
    }

    @Test
    void dbFixCreated_sendsOnePayloadPerApprover() {
        User approver1 = User.builder().id(UUID.randomUUID()).email("a1@x.com").role(Role.APPROVER).fullName("A1").build();
        User approver2 = User.builder().id(UUID.randomUUID()).email("a2@x.com").role(Role.APPROVER).fullName("A2").build();
        when(userRepository.findByRole(Role.APPROVER)).thenReturn(List.of(approver1, approver2));

        notificationService.notifyDbFixCreated(ticket);

        verify(restTemplate, times(2)).postForEntity(eq("https://example.com/webhook"), any(), eq(String.class));
    }

    @Test
    void assigned_sendsExactlyOnePayload_toAssignee() {
        notificationService.notifyAssigned(ticket);

        verify(restTemplate, times(1)).postForEntity(eq("https://example.com/webhook"), any(), eq(String.class));
    }

    @Test
    void closed_sendsToRequestorAndAssignee() {
        notificationService.notifyClosed(ticket);

        verify(restTemplate, times(2)).postForEntity(eq("https://example.com/webhook"), any(), eq(String.class));
    }

    @Test
    void webhookFailure_isSwallowed_doesNotThrow() {
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenThrow(new RestClientException("boom"));

        notificationService.notifyAssigned(ticket);
        // no exception propagated -- verified by reaching this line
    }

    @Test
    void blankWebhookUrl_skipsSendingEntirely() {
        ReflectionTestUtils.setField(notificationService, "webhookUrl", "");

        notificationService.notifyAssigned(ticket);

        verify(restTemplate, never()).postForEntity(any(String.class), any(), eq(String.class));
    }
}
