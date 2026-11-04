# API

Base URL `http://localhost:8080`. Everything except `/auth/register`, `/auth/login`, `/auth/refresh` and
`/actuator/health` needs
`Authorization: Bearer <access token>`.

Success: `{ "success": true, "message": "…", "data": … }`.
Error: `{ "success": false, "message": "…", "code": "…" }` with a 4xx or 5xx status.

## Auth (`/auth`)

| Method | Path | Body |
|---|---|---|
| POST | `/auth/register` | `{name, email, password}` |
| POST | `/auth/login` | `{email, password}` |
| POST | `/auth/refresh` | `{token}`. Returns a new pair; the old refresh token stops working |
| POST | `/auth/logout` | |
| GET | `/auth/me` | |

## Workspaces

| Method | Path | Notes |
|---|---|---|
| GET, POST | `/workspaces` | |
| GET, PUT, DELETE | `/workspaces/{id}` | |
| GET | `/workspaces/{id}/members` | |
| POST | `/workspaces/{id}/members/invite` | `{email, role}` |
| PUT, DELETE | `/workspaces/{id}/members/{userId}` | PUT `{role}` |
| GET | `/workspaces/{workspaceId}/tasks/assigned` | Your tasks in this workspace |

## Projects (`/workspaces/{workspaceId}/projects`)

| Method | Path | Notes |
|---|---|---|
| GET, POST | `/projects` | |
| GET, PUT, DELETE | `/projects/{id}` | |
| GET, POST | `/projects/{id}/members` | POST `{userId}` |
| DELETE | `/projects/{id}/members/{userId}` | |
| GET | `/projects/{id}/analytics` | |

## Tasks (`/workspaces/{workspaceId}/projects/{projectId}/tasks`)

| Method | Path | Notes |
|---|---|---|
| GET, POST | `/tasks` | `{title, description, type, status, priority, assigneeId, dueDate}` |
| GET, PUT, DELETE | `/tasks/{id}` | |
| GET, POST | `/tasks/{id}/comments` | POST `{content}` |
| DELETE | `/tasks/{id}/comments/{commentId}` | |

`type`: `TASK`, `BUG`, `FEATURE`, `IMPROVEMENT`, `OTHER`. `priority`: `LOW`, `MEDIUM`, `HIGH`.
`status`: `TODO`, `IN_PROGRESS`, `REVIEW`, `DONE`.

## Dashboard (`/workspaces/{workspaceId}/dashboard`)

`GET /stats`, `GET /recent-activity`, `GET /tasks-summary`, `GET /project-overview`.

## Health

`GET /actuator/health`.
