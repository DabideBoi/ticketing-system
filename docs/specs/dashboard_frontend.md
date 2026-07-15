# Frontend Specification: Timeline-First Dashboard

## Objective
Give every role a single landing page that leads with recent activity rather than a static table, per the "Timeline-first" (1c) wireframe direction.

## Component Specifications
- **KPI chips row:** 2-3 role-relevant counts pulled from `GET /api/dashboard/stats` (e.g. Requestor: "My open tickets"; Approver: "Pending approvals"; Assigner: "For assignment"; Assignee: "Assigned to me"/"Ongoing"; Admin: all of the above).
- **Activity feed:** Vertical timeline (colored dot per event + connecting rail) of the user's role-relevant recent ticket events, most recent first — ticket id, ticket type, transition, actor, relative time. Each entry links to `app/tickets/[id]`.
- **Empty state:** When there is no relevant activity, show a simple "Nothing to review right now" message instead of an empty rail.

## Test Checklist
- [ ] KPI chip values match the counts returned by `GET /api/dashboard/stats` for the logged-in role.
- [ ] Clicking an activity feed entry navigates to the correct ticket detail page.
- [ ] The feed only shows events relevant to the logged-in user's role/tickets (no cross-tenant leakage).
