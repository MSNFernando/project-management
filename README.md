# SpringPM

A multi-tenant project management platform: workspaces, projects, tasks, comments, a dashboard and a team view.

- **Backend:** Spring Boot 4.0 on Java 17, PostgreSQL 16, Spring Security with JWT.
- **Frontend:** React 19, Vite 7, Tailwind CSS 4, Redux Toolkit.

> **This repository is an early snapshot and isn't maintained.** Development continues in a private
> repository. This copy stops after the first round of fixes: working create endpoints, workspace roles and
> invites, project members and settings, deep links, single-use refresh tokens, and closing error leaks, CORS
> and per-request tenant state. It doesn't include the later work (Kanban board, two-factor sign-in, device
> sessions, Flyway migrations).

## Quick start (without Docker)

You need Java 17, Node 22 and PostgreSQL 16.

```bash
# 1. Database, with the schema from docker/postgres/init.sql
sudo -u postgres psql -c "CREATE USER springpm_user WITH PASSWORD 'change-me';"
sudo -u postgres psql -c "CREATE DATABASE springpm OWNER springpm_user;"
sudo -u postgres psql -d springpm -f docker/postgres/init.sql
sudo -u postgres psql -d springpm -c "GRANT ALL ON ALL TABLES IN SCHEMA public TO springpm_user;"

# 2. API on :8080
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/springpm DB_USER=springpm_user DB_PASSWORD=change-me
export JWT_SECRET="$(openssl rand -base64 48)" ADMIN_PASSWORD='choose-a-password'
./gradlew bootRun

# 3. Web app on :5173
cd ../frontend
npm ci
VITE_API_URL=http://localhost:8080 npm run dev
```

Sign in as `admin@springpm.com` with the `ADMIN_PASSWORD` you set.

## With Docker

```bash
cp .env.example .env      # change every password and secret in it
docker compose up --build
```

The web app is on http://localhost:3000 and the API on http://localhost:8080.

## Docs

| Doc | What's in it |
|---|---|
| [Getting started](docs/getting-started.md) | Configuration, running, tests, known issues |
| [Architecture](docs/architecture.md) | How the backend and frontend are laid out, multi-tenancy |
| [API](docs/api.md) | The endpoints and the response shape |
| [Project scope](docs/Project_Scope.md) | The original brief |
| [TODO](TODO.md) | The original task list |

## Repository layout

```
backend/     Spring Boot API (Gradle)
frontend/    React web app (Vite)
docker/      Postgres schema (init.sql)
docs/        Documentation
docker-compose.yml
```
