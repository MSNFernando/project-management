# SpringPM - Project Management Platform TODO

## Phase 1: Core Backend (✅ COMPLETE)
- [x] Database schema design with PostgreSQL
- [x] Entity layer (User, Workspace, Project, Task, TaskComment, etc.)
- [x] Repository layer with Spring Data JPA
- [x] Service layer with business logic
- [x] REST API controllers (Auth, Workspace, Project, Task, Dashboard)
- [x] JWT authentication and token management
- [x] Multi-tenant workspace isolation with TenantContext
- [x] Error handling and exception mapping
- [x] Request/Response DTOs

## Phase 2: Infrastructure & Deployment (✅ COMPLETE)
- [x] Docker setup for all 7 services (postgres, redis, kafka, zookeeper, minio, api, web)
- [x] Docker Compose orchestration
- [x] Frontend Dockerfile with Nginx production setup
- [x] Backend Dockerfile with Gradle build
- [x] Environment configuration (.env.dev)
- [x] PostgreSQL init.sql schema
- [x] Kafka topics creation (user-events, order-events)
- [x] Health checks for all services
- [x] Fix Kafka LISTENER_SECURITY_PROTOCOL_MAP configuration

## Phase 3: Frontend API Integration (🔄 IN PROGRESS)
- [x] Create API service layer with Axios
- [x] JWT token management (storage, refresh, interceptors)
- [x] Login page component
- [x] Signup page component
- [x] Auth context/Redux integration
- [x] Protected routes and authentication guards
- [ ] Replace dummy data with real API calls
  - [ ] Dashboard
  - [ ] Projects list and details
  - [ ] Tasks CRUD
  - [ ] Team management
  - [ ] Workspace management
- [x] Error handling and toast notifications
- [ ] Loading states and spinners

## Phase 4: Payment Integration (🎯 PLANNED)
- [ ] Stripe integration
- [ ] Subscription plans:
  - Pro Individual: 5 workspaces max, personal use
  - Team: Minimum 5 members, 10 workspaces max
  - Unlimited: Minimum 10 members, unlimited workspaces
- [ ] Billing dashboard
- [ ] Invoice management
- [ ] Payment history

## Phase 5: Advanced Features (🎯 PLANNED)
### AI Integration
- [ ] Claude API integration for project management assistance
- [ ] AI change request generator
- [ ] AI project reporting
- [ ] AI project coordination and timeline analysis
- [ ] Document analysis (PDF, Markdown)

### Additional Features
- [ ] Email notifications (Thymeleaf + Java Mail)
- [ ] File upload to MinIO
- [ ] Audit logging
- [ ] Activity timeline
- [ ] Real-time updates via Kafka events
- [ ] Advanced analytics and reporting

## Phase 6: Testing & Quality (🎯 PLANNED)
- [ ] Unit tests for services
- [ ] Integration tests for controllers
- [ ] Frontend component tests
- [ ] E2E tests
- [ ] Performance testing
- [ ] Security audit

## Phase 7: Monitoring & Operations (🎯 PLANNED)
- [ ] Prometheus metrics
- [ ] Logging setup
- [ ] Health monitoring dashboard
- [ ] Auto-scaling configuration

## Current Branch
- Feature branch: `feature/frontend-api-integration`
- Working on Login/Signup components and API service layer

## Notes
- All 7 Docker services are running and healthy (as of March 4, 2026)
- Backend Gradle build successful with Java 17
- Frontend containerized with Nginx SPA routing
- Multi-tenancy implemented with workspace isolation
