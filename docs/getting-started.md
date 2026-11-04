# Getting started

## Requirements

| Tool | Version |
|---|---|
| Java (JDK) | 17 |
| Node.js | 22 |
| PostgreSQL | 16 |
| Gradle | none needed; use `backend/gradlew` |

## Database

The schema is in `docker/postgres/init.sql`; Hibernate only checks it (`ddl-auto=validate`) and never changes
it. Docker Compose loads it into a new database automatically. Without Docker, run it yourself:

```bash
sudo -u postgres psql -c "CREATE USER springpm_user WITH PASSWORD 'change-me';"
sudo -u postgres psql -c "CREATE DATABASE springpm OWNER springpm_user;"
sudo -u postgres psql -d springpm -f docker/postgres/init.sql
sudo -u postgres psql -d springpm -c "GRANT ALL ON ALL TABLES IN SCHEMA public TO springpm_user;"
```

## Configuration

The API reads environment variables:

| Variable | Needed | What it does |
|---|---|---|
| `DB_URL` | yes | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/springpm` |
| `DB_USER`, `DB_PASSWORD` | yes | Database login |
| `JWT_SECRET` | yes | Signs access tokens. At least 32 characters: `openssl rand -base64 48` |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | first start | The admin user created on first start. Change the default password |
| `APP_CORS_ALLOWED_ORIGINS` | if the web app is on another origin | Comma-separated origins the browser may call the API from. Default `http://localhost:5173,http://localhost:3000` |
| `JWT_EXPIRATION_MS` | no | Access token lifetime. Default 900000 (15 minutes) |
| `JWT_REFRESH_EXPIRATION_MS` | no | Refresh token lifetime. Default 604800000 (7 days) |
| `LOGGING_LEVEL_COM_SPRINGPM` | no | `DEBUG`, `INFO` (default), `WARN` or `ERROR` |
| `MINIO_ENDPOINT`, `MINIO_USER`, `MINIO_PASSWORD` | no | Object storage; not used by the app yet |
| `ANTHROPIC_API_KEY` | no | Enables the Spring AI Anthropic client; not used by the app yet |

The web app reads `VITE_API_URL` at build or dev-server start: where the browser sends API calls (default
`http://localhost:8080`).

The values in `.env.example` are examples. Replace every password and secret before running it anywhere but your
own machine.

## Run

```bash
cd backend && ./gradlew bootRun                 # API on :8080; GET /actuator/health answers when ready
cd frontend && npm ci && npm run dev            # web app on :5173
```

## Tests and checks

```bash
cd backend && ./gradlew test      # JWT filter and error handling tests
cd frontend && npm run lint
cd frontend && npm run build
```

## Known issues

- The dashboard's completed counts look for the status `COMPLETED`, but tasks use `DONE`, so they always show 0.
- Tasks are a table and a calendar; there's no board view.
- Sign-in has no rate limit and no two-factor option, and there's no password reset.
