# Project Scope
We're designing a Project Management platform, where individuals and/or teams can manage projects.
The Frontend has been designed by another engineer.
They've build the platform to work off of static asset files, the frontend requires refactoring to ensure all actions flow through the backend. The frontend/backend should be stateless and allow the solution to scale efficiently.

# Architecture
The solution can run completly containerised and can be reconfigured for cloud deployment.
Zookeeper has been included in the project to allow scalable infrastrucrure.

- Backend:                  Spring Boot (Gradle, Groovy)
    - eMail templating:     Thymeleaf
    - Email Server:         Java Mail Sender
    - Security/Auth:        Spring Security
    - API Docs:             Spring REST Docs
- Frontend:                 React
- Database:                 Postgres
- Object Storage:           MinIO
- Messaging (event stream): Kafka
- Monitoring:               Prometheus

AI Functionality
- Claude integration for
    - PDF Document Reader
    - Markdown Document Reader

# First Steps
**Subagent - Project Manager and Consultant**
Read and understand the current state of the application, dig into the backend spring boot files and frontend files to write a comprehensive architectural and structural document. Read the docker-compose file which will also provide an overview of extra services used to build the full solution.
You'll need to build a ToDo list for subsiquent agents to follow.
Provide a changelog file where all changes can be rigerously documented before and after each change.
Ensure that all details are documented and documentation is always up-to-date.
You are the first agent that should spoolup and direct all other subagents to what tasks they are supposed to do.

- Explore the frontend and figure out what endpoints are needed to build a comprehensive backend (starting point project files are provided in the backend directory).
- The backend needs to be fully multi-tenantable with granular access controlls (frameworks have been provided for you to use), each endpont should have Read,Write,Edit permissions.
    - is there a API Gateway that can be added infront to provide security functions to the backend?
    - Permissions should be assignable to users and/or roles.
    - Users can be added to groups to make permission/role assignements easier.

# If you're unsure about any aspect of the project ask questions! Make sure you document any questions and answers.