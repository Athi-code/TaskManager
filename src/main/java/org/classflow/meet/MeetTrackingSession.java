
package org.classflow.meet;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "meet_tracking_sessions")
public class MeetTrackingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String meetingCode;

    private LocalDateTime joinTime;

    private LocalDateTime leaveTime;

    private String status;

   
    private Long trainingScheduleId;

    public MeetTrackingSession() {
    }

    public MeetTrackingSession(String meetingCode, LocalDateTime joinTime) {
        this.meetingCode = meetingCode;
        this.joinTime = joinTime;
        this.status = "IN_PROGRESS";
    }

    public Long getId() {
        return id;
    }

    public String getMeetingCode() {
        return meetingCode;
    }

    public void setMeetingCode(String meetingCode) {
        this.meetingCode = meetingCode;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }

    public LocalDateTime getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(LocalDateTime leaveTime) {
        this.leaveTime = leaveTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTrainingScheduleId() {
        return trainingScheduleId;
    }

    public void setTrainingScheduleId(Long trainingScheduleId) {
        this.trainingScheduleId = trainingScheduleId;
    }

    public Long getDurationSeconds() {
        if (joinTime == null || leaveTime == null) {
            return null;
        }

        return Duration.between(joinTime, leaveTime).getSeconds();
    }
}
