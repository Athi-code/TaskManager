# CLASSFLOW API Documentation

Base URL: `http://localhost:8082`

All routes below are based on the supplied controller source. Unless otherwise noted, request and response bodies use JSON.

## Task API

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/tasks` | List all tasks |
| GET | `/tasks/{id}` | Get one task |
| POST | `/tasks` | Create a task |
| PUT | `/tasks/{id}` | Update a task |
| DELETE | `/tasks/{id}` | Delete a task |
| GET | `/tasks/{id}/overdue` | Check overdue state |
| GET | `/tasks/{id}/deadline-warning` | Get deadline warning |
| GET | `/tasks/{id}/start-delay` | Get start-delay days |
| GET | `/tasks/{id}/start-delayed` | Check whether start is delayed |
| GET | `/tasks/{id}/delay` | Get completion-delay days |
| GET | `/tasks/{id}/dependents` | List tasks that depend on this task |
| GET | `/tasks/{id}/impact` | Get a basic dependent-task impact report |
| GET | `/tasks/{id}/dependency` | Check the task's prerequisite status |

### Task create/update example

`POST /tasks`

```json
{
  "title": "Prepare Java module",
  "description": "Complete the service layer",
  "status": "PENDING",
  "deadline": "2026-11-15",
  "plannedStartDate": "2026-11-01",
  "actualStartDate": null,
  "plannedCompletionDate": "2026-11-12",
  "actualCompletionDate": null,
  "dependsOnTaskId": null
}
```

Date fields use `YYYY-MM-DD`. The `status` field is a string; logic specifically checks for `COMPLETED`.

### Task behavior notes

- `overdue` is true when the deadline is before today and status is not `COMPLETED`.
- Deadline warning can return `COMPLETED`, `NO_DEADLINE`, `OVERDUE`, `DUE_SOON`, or `ON_TRACK`.
- Start-delay calculation uses the actual start date when present; otherwise it compares the planned start date with today's date.
- Completion-delay calculation returns a positive delay only when both planned and actual completion dates are present and actual completion is later.
- Unknown task IDs are not handled consistently by all endpoints. For example, some endpoints return `0` or `false`, and get/update can result in a null response. This should be improved before production use.
- `DELETE /tasks/{id}` returns plain text `"Task deleted successfully"` or `"Task not found"`.

## Class session API

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/classes` | Create a class session |
| GET | `/classes` | List all class sessions |
| GET | `/classes/{id}` | Get one class session |
| PATCH | `/classes/{id}/start` | Record session start time |
| PATCH | `/classes/{id}/confirm-missed` | Confirm a missed session |
| GET | `/classes/{id}/delay-impact` | Get missed-session/recovery-gap information |
| PATCH | `/classes/{id}/recovery` | Schedule recovery for a confirmed missed session |
| GET | `/classes/{id}/recovery` | Get recovery details |
| PATCH | `/classes/{id}/dependency` | Set or remove prerequisite session |
| GET | `/classes/{id}/dependency-impact` | Inspect possible dependency schedule impact |

### Create a class session

`POST /classes`

```json
{
  "topic": "Spring Boot Basics",
  "meetLink": "https://meet.google.com/example",
  "scheduledStart": "2026-11-02T10:00:00",
  "scheduledEnd": "2026-11-02T11:00:00"
}
```

Date-time fields use `YYYY-MM-DDTHH:mm:ss`.

### Record session start

`PATCH /classes/{id}/start`

No request body is required.

### Confirm a missed session

`PATCH /classes/{id}/confirm-missed`

No request body is required. The service rejects confirmation if a start has already been recorded.

### Schedule recovery

`PATCH /classes/{id}/recovery`

```json
{
  "recoveryDateTime": "2026-11-03T10:00:00",
  "recoveryNotes": "Catch-up session with the trainer"
}
```

Recovery is accepted only after the session is confirmed missed, and the recovery date-time must be in the future.

### Set or remove a prerequisite

`PATCH /classes/{id}/dependency`

Set prerequisite:

```json
{
  "prerequisiteId": 3
}
```

Remove prerequisite:

```json
{
  "prerequisiteId": null
}
```

The service rejects self-dependency, nonexistent prerequisite IDs, and circular dependency chains.

## Typical response/error behavior

- Successful Task responses vary by endpoint: JSON task, JSON list/map, boolean, number, or plain text.
- Successful ClassSession create/update actions generally return the updated session as JSON; impact endpoints return plain text.
- The global exception handler maps `IllegalArgumentException` and `IllegalStateException` to HTTP 400. It maps a `RuntimeException` whose message starts with `Class session not found:` to HTTP 404. Other runtime exceptions become HTTP 500 with a generic message.
- Task endpoints do not yet share the same consistent error-handling approach as ClassSession endpoints.

## Manual test suggestions

1. `GET /tasks`
2. `POST /tasks` using the example payload
3. `GET /tasks/{newId}/deadline-warning`
4. `GET /tasks/{newId}/impact`
5. `POST /classes`
6. `PATCH /classes/{id}/confirm-missed`
7. `PATCH /classes/{id}/recovery` with a future date-time
8. `PATCH /classes/{id}/dependency` to set a prerequisite
9. Try self-dependency and a circular dependency to confirm rejection
10. Remove the dependency with `{"prerequisiteId": null}` and verify `GET /classes/{id}/dependency-impact`
