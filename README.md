# FarmLink: Local Produce Marketplace

Direct farm-to-buyer marketplace (IT342 project). This repository currently contains
**Project Development Increment 1, Part 1: User Accounts and Authentication**.

| Layer     | Technology                                        |
|-----------|---------------------------------------------------|
| Frontend  | React 18 + Vite + React Router                    |
| Backend   | Spring Boot 3.4 (Java 17), Spring Security 6, JPA |
| Database  | PostgreSQL                                        |

## Implemented features

1. **User account creation**: farmer and buyer registration with role-specific profile fields,
   server- and client-side validation, duplicate-email prevention (checked in code and enforced by a
   unique database constraint), BCrypt password hashing, role assignment.
2. **Login and logout**: email/password login against the database, generic error for invalid
   credentials, server-side session (HttpOnly, SameSite=Lax cookie, 30-minute timeout, session id
   rotated on login), logout that invalidates the session and deletes the cookie, CSRF protection.
3. **Role-based dashboards**: Farmer, Buyer, and Admin dashboards; users are redirected to the
   dashboard for their role after login; role-specific navigation; unauthorized pages show 403 in
   the UI and the API returns 401/403, enforced by Spring Security on every route.

Admins cannot self-register. One admin account is created in the database on first startup
from `ADMIN_EMAIL` / `ADMIN_PASSWORD` (password is BCrypt-hashed; nothing is hardcoded in source).

## Prerequisites

- JDK 17 or newer
- Maven 3.9+
- Node.js 18+ and npm
- PostgreSQL 14+

## Setup and run

### 1. Database

```sql
CREATE DATABASE farmlink;
```

Tables are created automatically on first start (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Backend (http://localhost:8080)

```bash
cd backend
cp .env.example .env        # then edit DB_PASSWORD, ADMIN_PASSWORD
mvn spring-boot:run
```

`.env` is git-ignored. You can use real environment variables instead.

| Variable         | Purpose                                   | Default                                  |
|------------------|-------------------------------------------|------------------------------------------|
| `DB_URL`         | JDBC URL                                  | `jdbc:postgresql://localhost:5432/farmlink` |
| `DB_USERNAME`    | Database user                             | `postgres`                               |
| `DB_PASSWORD`    | Database password                         | (empty)                                  |
| `ADMIN_EMAIL`    | Bootstrap admin email                     | `admin@farmlink.local`                   |
| `ADMIN_PASSWORD` | Bootstrap admin password (no admin if empty) | (empty)                               |
| `COOKIE_SECURE`  | Set `true` when served over HTTPS         | `false`                                  |

### 3. Frontend (http://localhost:5173)

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` to the backend, so the session and CSRF cookies work
without CORS configuration. Open http://localhost:5173.

## Demo walkthrough

1. Register a **Farmer** and a **Buyer** at `/register`, then log in at `/login`.
2. Each lands on their own dashboard (`/farmer`, `/buyer`).
3. While logged in as a farmer, open `/buyer` or `/admin` and you get the 403 page.
4. Log in with the admin credentials from `.env` to see live user counts and the user list.
5. Click **Log out**, then try opening a dashboard URL; you are sent back to `/login`.

## API

| Method | Path                      | Access        | Description                         |
|--------|---------------------------|---------------|-------------------------------------|
| GET    | `/api/auth/csrf`          | public        | Issues the XSRF-TOKEN cookie        |
| POST   | `/api/auth/register`      | public        | Create a farmer or buyer account    |
| POST   | `/api/auth/login`         | public        | Authenticate, start a session       |
| POST   | `/api/auth/logout`        | authenticated | End the session                     |
| GET    | `/api/auth/me`            | authenticated | Current user                        |
| GET    | `/api/farmer/dashboard`   | FARMER        | Farmer dashboard data               |
| GET    | `/api/buyer/dashboard`    | BUYER         | Buyer dashboard data                |
| GET    | `/api/admin/dashboard`    | ADMIN         | User statistics and newest accounts |
| GET    | `/api/admin/users`        | ADMIN         | All users (no password data)        |

## Project structure

```
backend/   Spring Boot API (config, controller, dto, exception, model, repository, service)
frontend/  React app (pages, components, AuthContext, api.js)
```

## Notes

- Passwords are stored only as BCrypt hashes; API responses never include them.
- Farmer/buyer menu items for later modules (listings, cart, orders, sales) are shown disabled
  and are implemented in later increments.
