
package org.classflow.classsession;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;
    private String meetLink;

    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private LocalDateTime actualStartTime;

    private String status = "SCHEDULED";
    private boolean missedConfirmed = false;

    private String recoveryStatus = "PENDING";
    private LocalDateTime recoveryDateTime;
    private String recoveryNotes;

   
    private Long dependsOnSessionId;

    public ClassSession() {
    }

    public Long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    public LocalDateTime getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(LocalDateTime scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public LocalDateTime getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(LocalDateTime scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isMissedConfirmed() {
        return missedConfirmed;
    }

    public void setMissedConfirmed(boolean missedConfirmed) {
        this.missedConfirmed = missedConfirmed;
    }

    public String getRecoveryStatus() {
        return recoveryStatus;
    }

    public void setRecoveryStatus(String recoveryStatus) {
        this.recoveryStatus = recoveryStatus;
    }

    public LocalDateTime getRecoveryDateTime() {
        return recoveryDateTime;
    }

    public void setRecoveryDateTime(LocalDateTime recoveryDateTime) {
        this.recoveryDateTime = recoveryDateTime;
    }

    public String getRecoveryNotes() {
        return recoveryNotes;
    }

    public void setRecoveryNotes(String recoveryNotes) {
        this.recoveryNotes = recoveryNotes;
    }

    public Long getDependsOnSessionId() {
        return dependsOnSessionId;
    }

    public void setDependsOnSessionId(Long dependsOnSessionId) {
        this.dependsOnSessionId = dependsOnSessionId;
    }
}
