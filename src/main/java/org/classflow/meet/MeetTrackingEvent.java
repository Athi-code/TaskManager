package org.classflow.meet;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meet_tracking_events")
public class MeetTrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String meetingCode;

    private String eventType;

    private String source;

    private LocalDateTime detectedAt;

    public MeetTrackingEvent() {
    }

    public MeetTrackingEvent(
            String meetingCode,
            String eventType,
            String source,
            LocalDateTime detectedAt) {

        this.meetingCode = meetingCode;
        this.eventType = eventType;
        this.source = source;
        this.detectedAt = detectedAt;
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
}