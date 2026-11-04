# Architecture

```
Browser ──▶ web app (React, Vite or nginx) ──▶ Spring Boot API ──▶ PostgreSQL
```

The API is stateless (JWT access tokens, refresh tokens stored hashed in the database), so more than one instance
can run behind a load balancer.

## Backend (`backend/`)

Package `com.springpm.springpm`:

| Package | What's in it |
|---|---|
| `api.controller` | REST controllers: auth, workspaces, projects, tasks, assigned tasks, dashboard |
| `service` | Business logic |
| `security` | `JwtAuthenticationFilter`, `JwtTokenProvider`, user details |
| `config` | `SecurityConfig` (CORS, public paths), `DataInitializer` (first admin user) |
| `entity`, `repository` | JPA entities and Spring Data repositories |
| `exception` | `GlobalExceptionHandler`, which returns safe JSON errors (no stack traces or SQL) |
| `util` | `ApiResponse`, `TenantContext` |

### Multi-tenancy

Everything belongs to a workspace, and routes carry it (`/workspaces/{workspaceId}/…`). Services check that the
signed-in user is a member before reading or writing. `TenantContext` holds the current workspace for a request
and is cleared after every request. Workspace members have a role (`ADMIN` or `MEMBER`); projects have their own
member list.

### Tokens

- Access tokens are JWTs that last 15 minutes. Only access tokens authenticate API calls.
- Refresh tokens are random, stored as a hash, last 7 days, and work once: each refresh returns a new pair.

## Frontend (`frontend/`)

| Path | What's in it |
|---|---|
| `src/pages` | One file per route: Dashboard, Projects, ProjectDetails, TaskDetails, Team, Login, Signup, Layout |
| `src/components` | Shared pieces: task table, calendar, analytics, dialogs, sidebar |
| `src/services` | One module per API area; `api.js` is the axios instance that adds the token and refreshes it on 401 |
| `src/features`, `src/app` | Redux slices and the store |

Pages keep their state in the URL (`/projectsDetail?id=…&workspaceId=…&tab=…`), so they can be refreshed,
bookmarked and shared.
