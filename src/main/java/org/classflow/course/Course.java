
package org.classflow.course;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;

    private String trainerName;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime classStartTime;

    private LocalTime classEndTime;

    private int plannedDurationMinutes;

    private int totalPlannedSessions;

    public Course() {
    }

    public Long getId() {
        return id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getClassStartTime() {
        return classStartTime;
    }

    public void setClassStartTime(LocalTime classStartTime) {
        this.classStartTime = classStartTime;
    }

    public LocalTime getClassEndTime() {
        return classEndTime;
    }

    public void setClassEndTime(LocalTime classEndTime) {
        this.classEndTime = classEndTime;
    }

    public int getPlannedDurationMinutes() {
        return plannedDurationMinutes;
    }

    public void setPlannedDurationMinutes(int plannedDurationMinutes) {
        this.plannedDurationMinutes = plannedDurationMinutes;
    }

    public int getTotalPlannedSessions() {
        return totalPlannedSessions;
    }

    public void setTotalPlannedSessions(int totalPlannedSessions) {
        this.totalPlannedSessions = totalPlannedSessions;
    }
}
