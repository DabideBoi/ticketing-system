package com.ticketing.service;

import com.ticketing.entity.Role;
import com.ticketing.entity.Ticket;
import com.ticketing.entity.User;
import com.ticketing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${n8n.webhook-url}")
    private String webhookUrl;

    public void notifyDbFixCreated(Ticket ticket) {
        sendToRole(Role.APPROVER, ticket, "Approval Required", ticket.getRequestor());
    }

    public void notifyAssignmentRequired(Ticket ticket, User actionBy) {
        sendToRole(Role.ASSIGNER, ticket, "Ticket Assignment Required", actionBy);
    }

    public void notifyAssigned(Ticket ticket) {
        sendTo(ticket.getAssignee(), ticket, "Ticket Assigned", ticket.getAssignee());
    }

    public void notifyForClose(Ticket ticket) {
        sendTo(ticket.getRequestor(), ticket, "Ticket for Close", ticket.getAssignee());
    }

    public void notifyClosed(Ticket ticket) {
        sendTo(ticket.getRequestor(), ticket, "Ticket Close", ticket.getAssignee());
        if (ticket.getAssignee() != null) {
            sendTo(ticket.getAssignee(), ticket, "Ticket Close", ticket.getAssignee());
        }
    }

    private void sendToRole(Role role, Ticket ticket, String subjectLabel, User actionBy) {
        List<User> recipients = userRepository.findByRole(role);
        recipients.forEach(recipient -> sendTo(recipient, ticket, subjectLabel, actionBy));
    }

    private void sendTo(User recipient, Ticket ticket, String subjectLabel, User actionBy) {
        if (recipient == null) {
            return;
        }
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("N8N_WEBHOOK_URL not configured; skipping notification '{}' for ticket {}", subjectLabel, ticket.getId());
            return;
        }

        String subject = "Ticket Update: %s - %s".formatted(ticket.getId(), subjectLabel);
        String emailBody = """
                Hello %s,

                Ticket #%s has transitioned to %s.

                Details:
                Type: %s
                Action By: %s

                Please log in to take immediate action.""".formatted(
                recipient.getFullName(),
                ticket.getId(),
                subjectLabel,
                ticket.getType(),
                actionBy != null ? actionBy.getFullName() : "System"
        );

        Map<String, String> payload = Map.of(
                "subject", subject,
                "recipient", recipient.getEmail(),
                "emailBody", emailBody
        );

        try {
            restTemplate.postForEntity(webhookUrl, payload, String.class);
        } catch (Exception e) {
            log.error("Failed to send n8n notification for ticket {}: {}", ticket.getId(), e.getMessage());
        }
    }
}
