
package org.classflow.course;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "training_schedules")
public class TrainingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long courseId;

   
    private String meetingCode;

    private LocalDate sessionDate;

    private LocalTime scheduledStartTime;

    private LocalTime scheduledEndTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private Integer actualDurationMinutes;

    private String status;

    private boolean startReminderSent;

    private boolean followUpReminderSent;

    private String earlyClosureReason;

    private String topicName;

    private Boolean topicCompleted;

    private String topicNotes;

    public TrainingSchedule() {
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getMeetingCode() {
        return meetingCode;
    }

    public void setMeetingCode(String meetingCode) {
        this.meetingCode = meetingCode;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalTime getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(LocalTime scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    public LocalTime getScheduledEndTime() {
        return scheduledEndTime;
    }

    public void setScheduledEndTime(LocalTime scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }

    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public Integer getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public void setActualDurationMinutes(Integer actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isStartReminderSent() {
        return startReminderSent;
    }

    public void setStartReminderSent(boolean startReminderSent) {
        this.startReminderSent = startReminderSent;
    }

    public boolean isFollowUpReminderSent() {
        return followUpReminderSent;
    }

    public void setFollowUpReminderSent(boolean followUpReminderSent) {
        this.followUpReminderSent = followUpReminderSent;
    }

    public String getEarlyClosureReason() {
        return earlyClosureReason;
    }

    public void setEarlyClosureReason(String earlyClosureReason) {
        this.earlyClosureReason = earlyClosureReason;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Boolean getTopicCompleted() {
        return topicCompleted;
    }

    public void setTopicCompleted(Boolean topicCompleted) {
        this.topicCompleted = topicCompleted;
    }

    public String getTopicNotes() {
        return topicNotes;
    }

    public void setTopicNotes(String topicNotes) {
        this.topicNotes = topicNotes;
    }
}
