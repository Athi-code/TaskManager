
package org.classflow;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String status;

    private LocalDate deadline;
    private LocalDate plannedStartDate;
    private LocalDate actualStartDate;
    private LocalDate plannedCompletionDate;
    private LocalDate actualCompletionDate;

    private Long dependsOnTaskId;

    public Task() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDate getPlannedStartDate() {
        return plannedStartDate;
    }

    public void setPlannedStartDate(LocalDate plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
    }

    public LocalDate getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(LocalDate actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public LocalDate getPlannedCompletionDate() {
        return plannedCompletionDate;
    }

    public void setPlannedCompletionDate(
            LocalDate plannedCompletionDate) {
        this.plannedCompletionDate = plannedCompletionDate;
    }

    public LocalDate getActualCompletionDate() {
        return actualCompletionDate;
    }

    public void setActualCompletionDate(
            LocalDate actualCompletionDate) {
        this.actualCompletionDate = actualCompletionDate;
    }

    public Long getDependsOnTaskId() {
        return dependsOnTaskId;
    }

    public void setDependsOnTaskId(Long dependsOnTaskId) {
        this.dependsOnTaskId = dependsOnTaskId;
    }

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    public boolean isOverdue() {
        return deadline != null
                && deadline.isBefore(LocalDate.now())
                && !isCompleted();
    }

    public boolean isStartDelayed() {
        return plannedStartDate != null
                && actualStartDate == null
                && plannedStartDate.isBefore(LocalDate.now())
                && !isCompleted();
    }

    public long calculateStartDelayDays() {

        if (plannedStartDate == null) {
            return 0;
        }

        LocalDate startDate;

        if (actualStartDate != null) {
            startDate = actualStartDate;
        } else {
            startDate = LocalDate.now();
        }

        if (startDate.isAfter(plannedStartDate)) {
            return ChronoUnit.DAYS.between(
                    plannedStartDate, startDate);
        }

        return 0;
    }

    public long calculateDelayDays() {

        if (plannedCompletionDate == null) {
            return 0;
        }

        LocalDate completionDate;

        if (actualCompletionDate != null) {
            completionDate = actualCompletionDate;
        } else if (!isCompleted()) {
            completionDate = LocalDate.now();
        } else {
            return 0;
        }

        if (completionDate.isAfter(plannedCompletionDate)) {
            return ChronoUnit.DAYS.between(
                    plannedCompletionDate, completionDate);
        }

        return 0;
    }

    public String getDeadlineWarning() {

        if (isCompleted()) {
            return "COMPLETED";
        }

        if (deadline == null) {
            return "NO_DEADLINE";
        }

        LocalDate today = LocalDate.now();

        if (deadline.isBefore(today)) {
            return "OVERDUE";
        }

        long daysRemaining = ChronoUnit.DAYS.between(
                today, deadline);

        if (daysRemaining <= 3) {
            return "DUE_SOON";
        }

        return "ON_TRACK";
    }
}
