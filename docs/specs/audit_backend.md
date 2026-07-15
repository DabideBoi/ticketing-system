# Backend Specification: Audit Logging

## Objective
Record an immutable trail of every meaningful ticket action so status history and accountability can be reconstructed and displayed as the hero of the ticket detail view.

## Component Specifications
1. **`entity/AuditLog`:** `id`, `ticket_id`, `action_by`, `action` (`STATUS_CHANGE`, `ASSIGNMENT`, `APPROVAL`), `from_status`, `to_status`, `remarks`, `timestamp`.
2. **`service/AuditLogService`:** `record(ticket, actor, action, fromStatus, toStatus, remarks)` — called by `TicketService` on every transition, never called directly from controllers. `getTrail(ticketId)` returns entries ordered by `timestamp` ascending.
3. **Exposure:** `GET /api/tickets/{id}` includes the full audit trail (via `AuditLogResponse` DTO) alongside the ticket payload.

## Test Checklist
- [ ] Every `TicketService` transition (create, approve, reject, assign, start, resolve, close) produces exactly one audit row.
- [ ] `from_status`/`to_status` are correct and non-null for status changes; `remarks` populated for approve/reject.
- [ ] `GET /api/tickets/{id}` returns entries in chronological order.
- [ ] Audit rows are never updated or deleted via any exposed endpoint (write-once).
