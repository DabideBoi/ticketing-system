# Backend Specification: n8n Notification Integration

## Objective
Fire a webhook call to an external n8n workflow on every ticket status transition, so email alerts can be sent without coupling the backend to an SMTP provider.

## Component Specifications
1. **`service/NotificationService`:** Builds the JSON payload below and POSTs it to `${N8N_WEBHOOK_URL}` (env var, never hardcoded) via `RestTemplate`/`WebClient`. Called by `TicketService` after each transition, inside the same method but **after** the DB commit point — a webhook failure is logged and swallowed, never rolls back or fails the ticket transition itself.
2. **Recipient resolution:** When the ticket has no specific user yet in the target role (e.g. "who is *the* Approver" or "who is *the* Assigner"), the service queries all `User`s with that role and sends one payload per recipient. Once a specific user is attached to the ticket (assignee, requestor), that exact person is notified.

## Payload Schema
```json
{
  "subject": "Ticket Update: [Ticket-ID] - [Status Name]",
  "recipient": "target_user_email@domain.com",
  "emailBody": "Hello [Full Name],\n\nTicket #[Ticket-ID] has transitioned to [Status].\n\nDetails:\nType: [Ticket Type]\nAction By: [User Full Name]\n\nPlease log in to take immediate action."
}
```

## Trigger Matrix
| Event | Recipient(s) | Subject |
|---|---|---|
| DB Fix created | all `APPROVER` users | Approval Required |
| Non-DB Fix created / DB Fix approved | all `ASSIGNER` users | Ticket Assignment Required |
| Assigned | the ticket's `assignee` | Ticket Assigned |
| Status → For Close | the ticket's `requestor` | Ticket for Close |
| Status → Close | the ticket's `requestor` and `assignee` | Ticket Close |

## Test Checklist
- [ ] Creating a DB Fix ticket posts one payload per Approver.
- [ ] Approving a DB Fix ticket posts one payload per Assigner.
- [ ] Assigning posts exactly one payload, addressed to the assignee.
- [ ] Marking For Close posts one payload to the requestor.
- [ ] Closing posts payloads to both requestor and assignee.
- [ ] A simulated webhook failure (mocked non-2xx / exception) does not prevent the ticket's status from persisting.
