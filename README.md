# FarmLink: Local Produce Marketplace

Direct farm-to-buyer marketplace (IT342 project). The repository currently contains:

- **Project Development Increment 1**: User Accounts and Authentication
- **Project Development Increment 2**: User Profile Management, Reset Password, Change Password

Roles follow the approved SRS: **Farmer** and **Buyer**.

| Layer     | Technology                                        |
|-----------|---------------------------------------------------|
| Frontend  | React 18 + Vite + React Router                    |
| Backend   | Spring Boot 3.4 (Java 17), Spring Security 6, JPA |
| Database  | PostgreSQL (local or hosted, e.g. Supabase)       |

## Implemented features

### Increment 1: Accounts and authentication

1. **User account creation**: farmer and buyer registration with role-specific fields, server- and
   client-side validation, duplicate-email prevention (code check plus unique database constraint),
   BCrypt password hashing, role assignment.
2. **Login and logout**: DB-backed login, generic error for invalid credentials, server-side session
   (HttpOnly, SameSite=Lax cookie, 30-minute timeout, session id rotated on login), logout that
   invalidates the session and deletes the cookie, CSRF protection.
3. **Role-based dashboards**: Farmer and Buyer dashboards, redirect by role after login, role-specific
   navigation, 403 for unauthorized pages, server-side authorization on every route.

### Increment 2: Profile and password management

4. **User profile management** (`/profile`): view and edit your own profile (full name, phone; farm name
   and location for farmers; buyer type and delivery address for buyers). Email and role are read-only.
   Input is validated in the browser and again on the server. The endpoints take **no user id**: they
   always act on the logged-in user taken from the session, so one user cannot read or change another
   user's profile.
5. **Reset password (forgot password)**: "Forgot password?" link on the login page. The user enters their
   email; if an account exists, a reset link is sent. The link carries a random 256-bit token that is
   - stored in the database only as a **SHA-256 hash**,
   - **expiring** (15 minutes by default),
   - **single use** (consumed atomically, so it cannot be replayed),
   - superseded by newer requests (only the latest link works).

   The response is identical whether or not the email exists (no account enumeration), and a new link
   can be requested at most once per minute per account. The new password is BCrypt-hashed.
6. **Change password** (`/profile`): requires the current password, validates and confirms the new one,
   and must differ from the current one. The old hash is overwritten, so the previous password can no
   longer be used to log in.

## Prerequisites

- JDK 17 or newer
- Maven 3.9+ (or IntelliJ IDEA with Maven support)
- Node.js 18+ and npm
- PostgreSQL 14+ (or a Supabase project)

## Setup and run

### 1. Database

Local PostgreSQL:

```sql
CREATE DATABASE farmlink;
```

Tables (`users`, `password_reset_tokens`) are created and updated automatically on start
(`spring.jpa.hibernate.ddl-auto=update`).

Supabase: use the **Session pooler** connection details for `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`, run
the backend once, then run this in the Supabase SQL editor so the tables are not exposed through
Supabase's public API:

```sql
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE password_reset_tokens ENABLE ROW LEVEL SECURITY;
```

### 2. Backend (http://localhost:8080)

```bash
cd backend
cp .env.example .env        # Windows: copy .env.example .env  (then edit DB_PASSWORD)
mvn spring-boot:run
```

`.env` is git-ignored. Real environment variables (for example set in an IntelliJ run configuration)
work too.

| Variable                          | Purpose                                              | Default                                     |
|-----------------------------------|------------------------------------------------------|---------------------------------------------|
| `DB_URL`                          | JDBC URL                                             | `jdbc:postgresql://localhost:5432/farmlink` |
| `DB_USERNAME`                     | Database user                                        | `postgres`                                  |
| `DB_PASSWORD`                     | Database password                                    | (empty)                                     |
| `FRONTEND_URL`                    | Base URL used in the reset link                      | `http://localhost:5173`                     |
| `MAIL_FROM`                       | Sender address of reset emails                       | `no-reply@farmlink.local`                   |
| `SPRING_MAIL_HOST`                | SMTP host (e.g. `smtp.gmail.com`). Enables real email | (unset = development mode)                  |
| `SPRING_MAIL_USERNAME` / `_PASSWORD` | SMTP login (Gmail: use an App Password)           | (unset)                                     |
| `RESET_TOKEN_TTL_MINUTES`         | Lifetime of a reset link                             | `15`                                        |
| `RESET_REQUEST_COOLDOWN_SECONDS`  | Minimum time between reset requests per account      | `60`                                        |
| `COOKIE_SECURE`                   | `true` when served over HTTPS                        | `false`                                     |

**Password reset email.** If `SPRING_MAIL_HOST` is set, the link is emailed to the account owner
(in a `.env` file you can write the same setting as `spring.mail.host=...`). If it is not set, the backend
runs in **development mode** and prints the link in its console:

```
WARN ... PasswordResetMailer : SMTP is not configured, so no email was sent. DEV MODE reset link for user@example.com: http://localhost:5173/reset-password?token=...
```

### 3. Frontend (http://localhost:5173)

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` to the backend, so the session and CSRF cookies work without CORS
configuration. Open http://localhost:5173.

## Demo walkthrough

**Increment 1**

1. Register a **Farmer** and a **Buyer** at `/register`, then log in at `/login`. Each lands on its own dashboard.
2. As the farmer, open `/buyer`: the 403 page is shown.
3. Log out, then open `/farmer`: you are sent to `/login`.

**Increment 2**

4. Log in, open **My Profile**, change your name/phone and farm or buyer details, and save. Reload the page:
   the changes persist. Submit an empty farm name (farmer) or an invalid phone to see validation errors.
5. In **Change password**, enter a wrong current password (error), then the correct one with a new password.
   Log out: the old password is rejected and the new one works.
6. On the login page click **Forgot password?**, enter your email, then open the reset link (from your inbox,
   or from the backend console in development mode). Choose a new password and log in with it.
7. Open the same reset link again: it is reported as invalid or expired (single use). A link older than
   15 minutes is rejected as well.

## API

| Method | Path                                  | Access        | Description                                  |
|--------|---------------------------------------|---------------|----------------------------------------------|
| GET    | `/api/auth/csrf`                      | public        | Issues the XSRF-TOKEN cookie                 |
| POST   | `/api/auth/register`                  | public        | Create a farmer or buyer account             |
| POST   | `/api/auth/login`                     | public        | Authenticate, start a session                |
| POST   | `/api/auth/logout`                    | authenticated | End the session                              |
| GET    | `/api/auth/me`                        | authenticated | Current user                                 |
| POST   | `/api/auth/forgot-password`           | public        | Request a reset link (same reply for any email) |
| POST   | `/api/auth/reset-password/validate`   | public        | Check whether a reset token is still valid   |
| POST   | `/api/auth/reset-password`            | public        | Set a new password using a valid token       |
| GET    | `/api/profile`                        | FARMER, BUYER | Own profile                                  |
| PUT    | `/api/profile`                        | FARMER, BUYER | Update own profile                           |
| POST   | `/api/profile/password`               | FARMER, BUYER | Change own password (current password required) |
| GET    | `/api/farmer/dashboard`               | FARMER        | Farmer dashboard data                        |
| GET    | `/api/buyer/dashboard`                | BUYER         | Buyer dashboard data                         |

## Project structure

```
backend/   Spring Boot API (config, controller, dto, exception, model, repository, service)
frontend/  React app (pages, components, AuthContext, api.js, validation.js)
docs/      Increment completion reports
```

## Security notes

- Passwords are stored only as BCrypt hashes; API responses never include them.
- Reset tokens are random (SecureRandom, 256 bits), stored only as SHA-256 hashes, expire, and are single use.
- Profile and password endpoints identify the user from the session, never from a request parameter.
- All state-changing requests require a CSRF token.
- Menu items for later modules (listings, search, cart, orders, sales) are shown disabled and are
  implemented in later increments.
