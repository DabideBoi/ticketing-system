# Frontend Specification: Ticket Lifecycle & Workflow UI

## Objective
Provide visual interfaces for submitting and modifying tickets while dynamically constraining actions based on the logged-in user's role, following the Timeline-first (1c) direction.

## Component Specifications
- **Create Ticket Form (`app/tickets/new`):** Single-scroll form. Ticket type selector (`Service Request`, `DB Fix`, `Mass Users`, `BCP Users`, `Incident Report`). Choosing `DB Fix` reveals an inline "What happens next" preview showing the routing chain (`Approver → Assigner → Assignee`); other types show `Assigner → Assignee`.
- **Ticket Detail (`app/tickets/[id]`):** Vertical progress rail on the left (one dot per status, filled/active/pending states) with the audit trail as the primary content on the right — each entry shows the action, actor, and relative timestamp. This is the "hero" of the page, not a collapsed sidebar.
- **Workflow State Controls (role-gated, rendered inline on ticket detail):**
  - `APPROVER`/`ADMIN` on a `FOR_APPROVAL` ticket: Approve / Reject buttons with a remarks textarea.
  - `ASSIGNER`/`APPROVER`/`ADMIN` on a `FOR_ASSIGNMENT` ticket: Assignee dropdown (populated from `GET /api/tickets/assignable-users` or equivalent user list) + Assign button.
  - `ASSIGNEE` on an `ASSIGNED` ticket: "Start Work" button (→ `ONGOING`); on `ONGOING`: "Resolve" button (→ `FOR_CLOSE`).
  - `REQUESTOR`/`ASSIGNER`/`ADMIN` on a `FOR_CLOSE` ticket: "Close" button (→ `CLOSE`).

## Test Checklist
- [ ] Requestor cannot see Approve/Reject/Assign controls on any ticket.
- [ ] Rejecting a DB Fix ticket renders status as `Close` and disables further actions.
- [ ] The progress rail reflects the ticket's current status accurately for every status value.
- [ ] Choosing `DB Fix` in Create Ticket shows the Approver-first routing preview; choosing any other type shows the Assigner-first preview.
