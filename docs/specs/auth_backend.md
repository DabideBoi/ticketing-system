# Backend Specification: Authentication & RBAC

## Objective
Implement a stateless JWT authentication system paired with custom Spring Security filters to enforce role-based access control across `REQUESTOR`, `APPROVER`, `ASSIGNEE`, `ASSIGNER`, `ADMIN`.

## Component Specifications
1. **Spring Security Configuration (`config/SecurityConfig`):** Stateless session policy, `/api/auth/**` permitted without auth, all other routes require a valid JWT. `@EnableMethodSecurity` for `@PreAuthorize`.
2. **User Details Service (`service/CustomUserDetailsService`):** Loads `User` entities by email, exposes role as a Spring Security authority (`ROLE_<role>`).
3. **JWT Service (`security/JwtService`):** Issues and validates signed JWTs (subject = user id, claims = email, role). Secret from `${JWT_SECRET}` env var.
4. **JWT Filter (`security/JwtAuthFilter`):** Extracts and validates the `Authorization: Bearer <token>` header, populates `SecurityContextHolder`.
5. **Dev seed data:** A `CommandLineRunner` active only under the `dev` profile creates one account per role (BCrypt-hashed password `password123`) so the app is testable without a registration flow:
   - `requestor@ticketing.local` / `REQUESTOR`
   - `approver@ticketing.local` / `APPROVER`
   - `assigner@ticketing.local` / `ASSIGNER`
   - `assignee@ticketing.local` / `ASSIGNEE`
   - `admin@ticketing.local` / `ADMIN`

## API Contracts
### `POST /api/auth/login`
- **Payload Schema:**
```json
{
  "email": "user@test.com",
  "password": "plain_text_string"
}
```
- **Responses:**
  - `200 OK`: `{ "token": "jwt_token_string", "user": { "id", "email", "role", "fullName" } }`
  - `401 Unauthorized`: standardized error payload, "Invalid credentials".

## Test Checklist
- [ ] Successful login with a seeded user retrieves an active token.
- [ ] Login with incorrect password throws `401 Unauthorized`.
- [ ] Accessing `/api/tickets` without a token throws `401/403`.
- [ ] A token minted for one role cannot call an endpoint `@PreAuthorize`-gated to another role.
