# Frontend Specification: Authentication

## Objective
Provide a login experience and persist auth state across the app via Context API, gating every protected route.

## Component Specifications
- **Login Page (`app/login`):** Email + password form, calls `POST /api/auth/login`, stores `{ token, user }` via `AuthContext`, redirects to `/dashboard`.
- **AuthContext (`hooks/useAuth`, `lib/auth-context.tsx`):** Holds `user`, `token`, `login()`, `logout()`. Persists the token to `localStorage` and rehydrates on load.
- **API Client (`lib/api.ts`):** Fetch wrapper that attaches `Authorization: Bearer <token>` to every request and redirects to `/login` on `401`.
- **Route Guard:** A client-side wrapper/layout that redirects unauthenticated users to `/login`, and redirects users to `/dashboard` if they hit a role-gated page they can't use.

## Test Checklist
- [ ] Submitting valid credentials redirects to the dashboard.
- [ ] Submitting invalid credentials shows a toast error and stays on `/login`.
- [ ] Visiting a protected route while logged out redirects to `/login`.
- [ ] Logout clears the token and redirects to `/login`.
