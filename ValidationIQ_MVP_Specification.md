# ValidationIQ MVP Specification

## 1. Project Overview

**Project Name:** ValidationIQ

**Project Type:** Full-stack web application

**Primary Goal:**  
Build a simple test failure tracking application for software validation teams. The application should allow users to create projects, record test runs, log failures, assign severity and status, and add comments.

This MVP should focus only on the core workflow needed to track validation failures. AI features, authentication, file uploads, analytics, and advanced dashboards are outside the MVP scope.

## 2. Problem the Application Solves

Software validation teams often manage test failures through spreadsheets, emails, chat messages, or disconnected tools. This can make it difficult to:

- See which failures are still open
- Connect failures to a specific project or test run
- Track severity and status
- Review failure details and comments
- Maintain a consistent record of validation issues

ValidationIQ provides one place to organize projects, test runs, and failures.

## 3. MVP Objectives

The MVP must allow a user to:

1. Create, view, edit, and delete projects
2. Create, view, edit, and delete test runs
3. Create, view, edit, and delete failures
4. Associate a test run with a project
5. Associate a failure with a test run
6. Set failure severity
7. Set failure status
8. Add comments to a failure
9. Search or filter failures
10. View a simple dashboard with key counts

The application should be usable by a single user or small team without authentication.

## 4. MVP Scope

### Included

- Project management
- Test run management
- Failure management
- Comments on failures
- Status and severity tracking
- Basic search and filtering
- Simple dashboard metrics
- Form validation
- Error handling
- PostgreSQL persistence
- Docker support
- Basic automated tests

### Not Included

The following features are intentionally excluded from the MVP:

- User authentication
- Role-based permissions
- AI-generated summaries
- AI classification
- File or log uploads
- Email notifications
- Real-time updates
- Advanced charts
- External integrations
- Mobile application
- Multi-tenant support
- Audit history
- Production-grade monitoring

These features may be added after the MVP is complete.

## 5. Recommended Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- Bean Validation
- Maven

### Frontend

- Thymeleaf
- HTML
- CSS
- Bootstrap

### Database

- PostgreSQL

### Testing

- JUnit 5
- Mockito
- Spring Boot Test

### Development and Packaging

- Docker
- Docker Compose
- Git
- GitHub or GitLab
- IntelliJ IDEA

### Optional Utilities

- Lombok
- Spring Boot DevTools
- DBeaver or pgAdmin
- Postman or Bruno

## 6. High-Level Architecture

Use a standard layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Controller Layer

Handles HTTP requests, form submissions, redirects, and page rendering.

### Service Layer

Contains business logic and coordinates repository operations.

### Repository Layer

Uses Spring Data JPA to communicate with PostgreSQL.

### Entity Layer

Represents database tables and relationships.

### View Layer

Uses Thymeleaf templates to display data and forms.

## 7. Core Entities and Database Tables

### 7.1 Project

Represents a software or validation project.

| Field | Type | Required | Description |
|---|---|---:|---|
| id | Long | Yes | Primary key |
| name | String | Yes | Project name |
| description | Text | No | Short project description |
| createdAt | LocalDateTime | Yes | Creation timestamp |
| updatedAt | LocalDateTime | Yes | Last update timestamp |

**Relationship:** One project can have many test runs.

### 7.2 TestRun

Represents a validation or regression test execution.

| Field | Type | Required | Description |
|---|---|---:|---|
| id | Long | Yes | Primary key |
| name | String | Yes | Test run name |
| description | Text | No | Test run notes |
| executionDate | LocalDate | Yes | Date the test run occurred |
| result | Enum | Yes | PASS, FAIL, or PARTIAL |
| projectId | Long | Yes | Related project |
| createdAt | LocalDateTime | Yes | Creation timestamp |
| updatedAt | LocalDateTime | Yes | Last update timestamp |

**Relationships:**

- Many test runs belong to one project
- One test run can have many failures

### 7.3 Failure

Represents an issue discovered during a test run.

| Field | Type | Required | Description |
|---|---|---:|---|
| id | Long | Yes | Primary key |
| title | String | Yes | Short failure title |
| description | Text | Yes | Detailed explanation |
| expectedResult | Text | No | Expected behavior |
| actualResult | Text | No | Actual behavior |
| severity | Enum | Yes | LOW, MEDIUM, HIGH, or CRITICAL |
| status | Enum | Yes | OPEN, IN_PROGRESS, RESOLVED, or CLOSED |
| testRunId | Long | Yes | Related test run |
| createdAt | LocalDateTime | Yes | Creation timestamp |
| updatedAt | LocalDateTime | Yes | Last update timestamp |

**Relationships:**

- Many failures belong to one test run
- One failure can have many comments

### 7.4 Comment

Represents a note added to a failure.

| Field | Type | Required | Description |
|---|---|---:|---|
| id | Long | Yes | Primary key |
| authorName | String | Yes | Name of the commenter |
| content | Text | Yes | Comment text |
| failureId | Long | Yes | Related failure |
| createdAt | LocalDateTime | Yes | Creation timestamp |

**Relationship:** Many comments belong to one failure.

## 8. Enums

### TestRunResult

```text
PASS
FAIL
PARTIAL
```

### FailureSeverity

```text
LOW
MEDIUM
HIGH
CRITICAL
```

### FailureStatus

```text
OPEN
IN_PROGRESS
RESOLVED
CLOSED
```

## 9. Page Requirements

### 9.1 Dashboard

**Route:** `/dashboard`

**Display:**

- Total projects
- Total test runs
- Total failures
- Open failures
- Critical failures
- Resolved failures
- Five most recently created failures
- Five most recent test runs

**Actions:**

- Navigate to projects, test runs, and failures
- Create a project, test run, or failure

Advanced charts are not required.

### 9.2 Projects List

**Route:** `/projects`

**Display:**

- Project name
- Short description
- Number of test runs
- Created date

**Actions:**

- View project
- Create project
- Edit project
- Delete project

Do not allow deletion when test runs exist unless cascade deletion is deliberately implemented and clearly confirmed.

### 9.3 Create/Edit Project

**Routes:**

```text
/projects/new
/projects/{id}/edit
```

**Fields:**

- Name
- Description

**Validation:**

- Name is required
- Name and description should have reasonable maximum lengths

### 9.4 Project Details

**Route:** `/projects/{id}`

**Display:**

- Name
- Description
- Created and updated dates
- Associated test runs

**Actions:**

- Edit or delete project
- Create a test run for the project
- Open a test run
- Return to the projects list

### 9.5 Test Runs List

**Route:** `/test-runs`

**Display:**

- Test run name
- Project
- Execution date
- Result
- Number of failures

**Filters:**

- Project
- Result
- Execution date, if simple to implement

### 9.6 Create/Edit Test Run

**Routes:**

```text
/test-runs/new
/test-runs/{id}/edit
```

**Fields:**

- Name
- Description
- Project
- Execution date
- Result

**Validation:** Name, project, execution date, and result are required.

### 9.7 Test Run Details

**Route:** `/test-runs/{id}`

**Display:**

- Name
- Description
- Project
- Execution date
- Result
- Created and updated dates
- Associated failures

**Actions:**

- Edit or delete test run
- Create failure for this test run
- Open failure details
- Return to the test runs list

### 9.8 Failures List

**Route:** `/failures`

**Display:**

- Failure title
- Test run
- Project
- Severity
- Status
- Created date

**Search:**

- Title
- Description

**Filters:**

- Status
- Severity
- Project
- Test run

Search and filters may use query parameters.

### 9.9 Create/Edit Failure

**Routes:**

```text
/failures/new
/failures/{id}/edit
```

**Fields:**

- Title
- Description
- Expected result
- Actual result
- Test run
- Severity
- Status

**Validation:**

- Title, description, test run, severity, and status are required
- Status defaults to OPEN
- Severity may default to MEDIUM

### 9.10 Failure Details

**Route:** `/failures/{id}`

**Display:**

- Title
- Description
- Expected result
- Actual result
- Project
- Test run
- Severity
- Status
- Created and updated dates
- Comments in chronological order

**Actions:**

- Edit or delete failure
- Add comment
- Return to failures list
- Open related test run or project

### 9.11 Add Comment Form

The form may appear directly on the failure details page.

**Route:** `/failures/{failureId}/comments`

**Fields:**

- Author name
- Comment content

Both fields are required. Editing and deleting comments are not required.

## 10. Navigation

Every main page should include links to:

- Dashboard
- Projects
- Test Runs
- Failures

## 11. Business Rules

1. Every test run must belong to a project.
2. Every failure must belong to a test run.
3. Every comment must belong to a failure.
4. New failures default to OPEN.
5. New failures may default to MEDIUM severity.
6. Required fields cannot be empty.
7. Invalid IDs must show a user-friendly error.
8. Test runs with failures should not be deleted without deliberate cascade support.
9. Projects with test runs should not be deleted without deliberate cascade support.
10. Deleting a failure should delete its comments.
11. Destructive actions should require confirmation.
12. Database errors must not expose stack traces to the user.

## 12. Suggested Routes

| Method | Route | Purpose |
|---|---|---|
| GET | / | Redirect to dashboard |
| GET | /dashboard | View dashboard |
| GET | /projects | List projects |
| GET | /projects/new | Show create project form |
| POST | /projects | Create project |
| GET | /projects/{id} | View project |
| GET | /projects/{id}/edit | Show edit project form |
| POST | /projects/{id} | Update project |
| POST | /projects/{id}/delete | Delete project |
| GET | /test-runs | List test runs |
| GET | /test-runs/new | Show create test run form |
| POST | /test-runs | Create test run |
| GET | /test-runs/{id} | View test run |
| GET | /test-runs/{id}/edit | Show edit test run form |
| POST | /test-runs/{id} | Update test run |
| POST | /test-runs/{id}/delete | Delete test run |
| GET | /failures | List and filter failures |
| GET | /failures/new | Show create failure form |
| POST | /failures | Create failure |
| GET | /failures/{id} | View failure |
| GET | /failures/{id}/edit | Show edit failure form |
| POST | /failures/{id} | Update failure |
| POST | /failures/{id}/delete | Delete failure |
| POST | /failures/{id}/comments | Add comment |

The exact route style may change as long as it remains consistent.

## 13. Suggested Package Structure

```text
src/main/java/com/example/validationiq/
├── controller/
├── dto/
├── entity/
├── enums/
├── exception/
├── repository/
├── service/
└── ValidationIqApplication.java
```

```text
src/main/resources/
├── static/
│   ├── css/
│   └── js/
├── templates/
│   ├── fragments/
│   ├── projects/
│   ├── test-runs/
│   ├── failures/
│   ├── dashboard.html
│   └── error.html
└── application.properties
```

## 14. Forms and Validation

Use DTOs or form objects when practical instead of binding all forms directly to entities.

Suggested form classes:

- ProjectForm
- TestRunForm
- FailureForm
- CommentForm

Use Bean Validation annotations such as:

```text
@NotBlank
@NotNull
@Size
```

Show validation messages beside the relevant fields.

## 15. Error Handling

Handle at least:

- Project not found
- Test run not found
- Failure not found
- Invalid form submission
- Attempted deletion of records with dependent data
- General server errors

A `ResourceNotFoundException` and a global `@ControllerAdvice` handler are recommended.

## 16. Testing Requirements

### Minimum Tests

- Project service create and update
- Test run creation and project relationship
- Failure creation and default status
- Failure filtering by status or severity
- Comment creation
- Validation failure for missing required fields
- Prevention of invalid deletion where applicable
- Basic application context test

Use unit tests for service logic, repository tests for custom queries, and at least one controller/form submission test.

## 17. Docker Requirements

Include:

- `Dockerfile`
- `docker-compose.yml`
- PostgreSQL service
- Application service
- Environment variables for database configuration

Suggested variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

The application should run with:

```bash
docker compose up --build
```

Do not commit secrets.

## 18. Seed Data

Optional development data:

### Projects

- Battery Calibration Validation
- Infotainment Regression
- Powertrain Software Testing

### Test Runs

- Battery Regression Run
- Infotainment Smoke Test
- Powertrain Calibration Verification

### Failures

- Voltage threshold exceeded
- Bluetooth reconnect test failed
- Calibration value did not persist after restart

Load seed data only in development or when the database is empty.

## 19. UI Expectations

### Required

- Consistent navigation
- Readable forms
- Clear action buttons
- Tables for list pages
- Status and severity labels
- Delete confirmation
- Basic responsive behavior
- Helpful empty-state messages

### Not Required

- Animations
- Complex charts
- Dark mode
- Heavy JavaScript
- Custom design system

Bootstrap is acceptable.

## 20. Definition of Done

The MVP is complete when:

- [ ] The project runs locally
- [ ] PostgreSQL stores application data
- [ ] Projects support create, view, edit, and delete
- [ ] Test runs support create, view, edit, and delete
- [ ] Failures support create, view, edit, and delete
- [ ] Comments can be added to failures
- [ ] Entity relationships work correctly
- [ ] Failure status and severity can be updated
- [ ] Failures can be searched or filtered
- [ ] The dashboard shows basic counts and recent items
- [ ] Required field validation works
- [ ] Invalid IDs and errors are handled gracefully
- [ ] Important business logic has automated tests
- [ ] The application runs through Docker Compose
- [ ] The repository includes setup instructions
- [ ] No unnecessary post-MVP features are included

## 21. Suggested Development Order

### Phase 1: Setup

1. Create the Spring Boot project
2. Configure PostgreSQL
3. Create the package structure
4. Add Docker Compose for PostgreSQL
5. Confirm the application starts

### Phase 2: Domain Model

1. Create enums
2. Create Project
3. Create TestRun
4. Create Failure
5. Create Comment
6. Create repositories
7. Verify database tables

### Phase 3: Projects

1. Project service
2. Project controller
3. Project list
4. Project form
5. Project details
6. Validation

### Phase 4: Test Runs

1. Test run service
2. Test run controller
3. Test run list
4. Test run form
5. Test run details
6. Project relationship

### Phase 5: Failures

1. Failure service
2. Failure controller
3. Failure list
4. Failure form
5. Failure details
6. Search and filters

### Phase 6: Comments and Dashboard

1. Comment service and form
2. Display comments
3. Dashboard queries
4. Dashboard page

### Phase 7: Quality and Packaging

1. Add exception handling
2. Add automated tests
3. Add deletion confirmation
4. Improve validation messages
5. Add seed data
6. Build the application Docker image
7. Complete the README

## 22. Instructions for an AI Coding Assistant

When assisting with this project:

1. Stay inside the MVP scope in this document.
2. Do not add authentication, AI features, uploads, notifications, or other post-MVP features unless explicitly requested.
3. Build one feature at a time.
4. State where every new file should be created.
5. Provide complete file contents when changes are substantial.
6. Keep code understandable for an early-career Java developer.
7. Use standard Spring Boot conventions.
8. Avoid unnecessary frameworks and architectural complexity.
9. Include commands needed to run or test each phase.
10. Identify required environment variables.
11. Recommend a Git commit after each meaningful feature.
12. Verify entity relationships before building views.
13. Add validation and error handling with each form.
14. Keep the application runnable throughout development.
15. Do not move to the next phase until the current phase builds and tests successfully.

## 23. Future Features After the MVP

Possible later enhancements, but not part of this MVP:

- Authentication and user accounts
- Assignment to registered users
- AI log summarization
- AI severity and category suggestions
- Similar failure detection
- Test log uploads
- Email or Teams notifications
- Audit history
- Metrics charts
- REST API for external clients
- React frontend
- Azure deployment
- CI/CD pipeline
- Application monitoring

The MVP should create a clean foundation for these features without building them now.
