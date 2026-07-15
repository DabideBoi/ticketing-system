# Frontend Specification: Reporting & Analytics

## Objective
Give `ADMIN`, `APPROVER`, and `ASSIGNER` roles a high-level analytical view of ticket volume and status distribution.

## Component Specifications
- **Reporting Page (`app/reporting`):** Gated route — non-permitted roles are redirected to `/dashboard`. Fetches `GET /api/reporting/summary`.
- **Status distribution chart:** Bar or donut chart mapping each `TicketStatus` to its current count.
- **Volume-over-time chart:** Ticket creation/close counts, if the summary payload includes a time series; otherwise a simple totals table.
- **Type breakdown:** Count of tickets per `TicketType`.

## Test Checklist
- [ ] `REQUESTOR` and `ASSIGNEE` roles cannot access `/reporting` (redirected).
- [ ] Chart values match the `GET /api/reporting/summary` payload.
- [ ] Page renders a reasonable empty/loading state before data arrives.
