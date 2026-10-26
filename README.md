# CLASSFLOW — Intelligent Training Workflow & Delay Impact Management System

## Project overview

CLASSFLOW is a Java backend application for tracking work tasks and scheduled class sessions. It helps users inspect task deadlines, calculate recorded start/completion delays, review task dependencies, confirm missed class sessions, schedule recovery sessions, and identify possible impact on dependent work or classes.

## Current technology

- Java (the project was configured for Java 21 in the supplied Eclipse metadata)
- Spring Boot web application
- Spring Data JPA / Hibernate
- MySQL
- Maven
- Postman for manual API testing

The application is configured to run on port `8082`.

## Current modules

### 1. Task module

- Create, list, read, update, and delete tasks
- Check whether a task is overdue
- Get deadline warning status
- Calculate start delay and completion delay
- Check whether a task has a prerequisite task
- List dependent tasks
- Return a basic impact report for incomplete dependent tasks

### 2. Class session module

- Create and list class sessions
- Retrieve a class session by ID
- Record that a session has started
- Confirm a missed session
- Schedule a future recovery session for a confirmed missed session
- View recovery details and recovery-gap information
- Configure or remove a prerequisite session
- Detect self-dependencies and circular dependencies
- Inspect possible schedule impact on a dependent session

## Prerequisites

1. Java installed (use the version configured by the project).
2. MySQL Server running.
3. Maven support in Eclipse, or Maven installed on the command line.
4. Postman (optional, for manual API testing).

## Database setup

Create the database in MySQL:

```sql
CREATE DATABASE taskmanager;
```

The supplied `application.properties` points to `jdbc:mysql://localhost:3306/taskmanager` and uses the MySQL `root` account. **Do not share database passwords or commit them to source control.** Before sharing or publishing the project, move credentials into environment variables or a local-only configuration file and rotate any password that has been exposed.

## Run in Eclipse

1. Extract the project.
2. In Eclipse, choose **File → Import → Existing Maven Projects**.
3. Select the folder containing `pom.xml`.
4. Wait for Maven dependencies to resolve.
5. Confirm MySQL is running and the `taskmanager` database exists.
6. Check the local database credentials in `src/main/resources/application.properties` (keep credentials private).
7. Run `TaskManagerApplication.java` as a Java application.
8. Confirm the console reports that the server started on port `8082`.

Base URL:

```text
http://localhost:8082
```

## API documentation

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for the implemented endpoint list and example request bodies.

## Important implementation notes

- The supplied project has no frontend; it is a backend REST API.
- The uploaded source did not include automated application tests. The project has been manually tested through Postman according to the developer's reported results.
- Some Task endpoints return fallback values such as `0`, `false`, `null`, or `"Task not found"` for unknown IDs rather than consistent HTTP error responses. This is a known improvement opportunity.
- The Task dependency is stored as a numeric ID (`dependsOnTaskId`), not as a JPA relationship. The ClassSession prerequisite is similarly stored as an ID.
- The delay-impact feature reports incomplete dependent tasks and possible impact. It does not currently calculate a full project completion forecast.
- Recovery-gap output explicitly distinguishes the time until recovery from actual course completion delay.
- Authentication, role-based access control, automated reminder scheduling, and frontend screens are not implemented in the supplied source and should not be described as completed features.

## Suggested demo flow

1. Start the application and show `GET /tasks` and `GET /classes`.
2. Create a task and demonstrate deadline/start/completion delay endpoints.
3. Demonstrate task dependency and `/impact`.
4. Create a class session, confirm a missed session, then schedule a future recovery.
5. Create a second session and configure a prerequisite.
6. Demonstrate dependency-impact output.
7. Show that self-dependency and circular dependency attempts are rejected.
8. Explain current limitations honestly and describe future enhancements.
