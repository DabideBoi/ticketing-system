# Backend Specification: Ticket Workflow Engine

## Objective
Enforce the ticket status state machine and RBAC transition rules server-side, writing an audit log entry and firing a notification on every transition.

## State Machine
```
Requestor creates ticket
  DB_FIX             -> FOR_APPROVAL -> (approve) -> FOR_ASSIGNMENT
                                      -> (reject)  -> CLOSE
  other types         -> FOR_ASSIGNMENT
FOR_ASSIGNMENT -> (assign)      -> ASSIGNED
ASSIGNED       -> (start work)  -> ONGOING
ONGOING        -> (resolve)     -> FOR_CLOSE
FOR_CLOSE      -> (close)       -> CLOSE
```
`OPEN` exists in the schema for completeness but is not entered by this flow — tickets land directly in `FOR_APPROVAL` or `FOR_ASSIGNMENT` on creation.

## Component Specifications
1. **`service/TicketService`:** One method per transition (`create`, `approve`, `assign`, `updateStatus`). Each method (a) validates the ticket's current status is a legal source state for the requested transition, (b) validates the caller's role against the RBAC grid, (c) mutates status + relevant FK (`approver_id`/`assignee_id`), (d) writes an `AuditLog` row, (e) invokes `NotificationService`.
2. **`controller/TicketController`:** Thin layer delegating to `TicketService`, mapping DTOs, `@PreAuthorize` per endpoint.

## RBAC Grid
| Role | Create | Approve/Reject DB Fix | Assign | Work (start/resolve) | Close |
|---|---|---|---|---|---|
| Requestor | Yes | No | No | No | Yes (own) |
| Approver | No | Yes | Yes | No | No |
| Assigner | No | No | Yes | No | Yes |
| Assignee | No | No | No | Yes | No |
| Admin | Yes | Yes | Yes | Yes | Yes |

## API Contracts
- `POST /api/tickets` (`REQUESTOR`, `ADMIN`) — `{ title, description, type }`
- `GET /api/tickets` (role-scoped: Requestor→own, Approver→FOR_APPROVAL, Assigner→FOR_ASSIGNMENT, Assignee→ASSIGNED/ONGOING, Admin→all)
- `GET /api/tickets/{id}` — ticket + audit trail
- `POST /api/tickets/{id}/approve` (`APPROVER`, `ADMIN`) — `{ approved, remarks }`
- `POST /api/tickets/{id}/assign` (`ASSIGNER`, `APPROVER`, `ADMIN`) — `{ assigneeId }`
- `PUT /api/tickets/{id}/status` (role-dependent per target status) — `{ status }`

## Test Checklist
- [ ] Creating a `DB_FIX` ticket lands in `FOR_APPROVAL`; any other type lands in `FOR_ASSIGNMENT`.
- [ ] Rejecting a `FOR_APPROVAL` ticket sets status to `CLOSE` and stops the flow.
- [ ] Approving moves a ticket to `FOR_ASSIGNMENT`.
- [ ] Assign transitions `FOR_ASSIGNMENT` → `ASSIGNED`; wrong source status is rejected.
- [ ] Assignee-only actions (`ONGOING`, `FOR_CLOSE`) fail for other roles.
- [ ] Close from `FOR_CLOSE` is restricted to `REQUESTOR`/`ASSIGNER`/`ADMIN`.
- [ ] Every transition produces exactly one new `AuditLog` row with correct `from_status`/`to_status`.
