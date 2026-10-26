
package org.classflow.meet;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.classflow.course.TrainingSchedule;
import org.classflow.course.TrainingScheduleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/meet-tracking")
@CrossOrigin(origins = "*")
public class MeetTrackingController {

    private final MeetTrackingRepository repository;
    private final MeetTrackingSessionRepository sessionRepository;
    private final TrainingScheduleRepository scheduleRepository;

    public MeetTrackingController(
            MeetTrackingRepository repository,
            MeetTrackingSessionRepository sessionRepository,
            TrainingScheduleRepository scheduleRepository) {

        this.repository = repository;
        this.sessionRepository = sessionRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public static class TrackingEventRequest {

        private String meetingCode;
        private String eventType;
        private String source;

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
    }

    @PostMapping("/events")
    @Transactional
    public MeetTrackingEvent recordEvent(
            @RequestBody TrackingEventRequest request) {

        if (request == null
                || request.getMeetingCode() == null
                || request.getMeetingCode().isBlank()
                || request.getEventType() == null
                || request.getEventType().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "meetingCode and eventType are required");
        }

        String meetingCode = request.getMeetingCode().trim();
        String eventType = request.getEventType()
                .trim()
                .toUpperCase(java.util.Locale.ROOT);

        String source = request.getSource() == null
                ? "EDGE_EXTENSION"
                : request.getSource();

        LocalDateTime now = LocalDateTime.now();

        if (!"MEETING_JOINED".equals(eventType)
                && !"MEETING_LEFT".equals(eventType)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "eventType must be MEETING_JOINED or MEETING_LEFT");
        }

        Optional<MeetTrackingSession> activeSession =
                sessionRepository
                        .findFirstByMeetingCodeAndStatusOrderByJoinTimeDesc(
                                meetingCode, "IN_PROGRESS");

        if ("MEETING_JOINED".equals(eventType)) {

            if (activeSession.isPresent()) {
                return repository.save(
                        new MeetTrackingEvent(
                                meetingCode, eventType, source, now));
            }

            TrainingSchedule matchingSchedule =
                    findMatchingSchedule(meetingCode, now);

            MeetTrackingSession session =
                    new MeetTrackingSession(meetingCode, now);

            if (matchingSchedule != null) {

                session.setTrainingScheduleId(matchingSchedule.getId());

                matchingSchedule.setActualStartTime(now);
                matchingSchedule.setStatus("IN_PROGRESS");

                scheduleRepository.save(matchingSchedule);
            }

            sessionRepository.save(session);

        } else {

            if (activeSession.isEmpty()) {
                return repository.save(
                        new MeetTrackingEvent(
                                meetingCode, eventType, source, now));
            }

            MeetTrackingSession session = activeSession.get();

            if (now.isBefore(session.getJoinTime())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Leave time cannot be before join time");
            }

            session.setLeaveTime(now);
            session.setStatus("COMPLETED");
            sessionRepository.save(session);

            Long scheduleId = session.getTrainingScheduleId();

            if (scheduleId != null) {

                Optional<TrainingSchedule> matchingSchedule =
                        scheduleRepository.findById(scheduleId);

                if (matchingSchedule.isPresent()) {

                    TrainingSchedule schedule = matchingSchedule.get();

                    if ("IN_PROGRESS".equals(schedule.getStatus())
                            && schedule.getActualStartTime() != null
                            && !now.isBefore(
                                    schedule.getActualStartTime())) {

                        schedule.setActualEndTime(now);

                        long durationMinutes = Duration.between(
                                schedule.getActualStartTime(), now)
                                .toMinutes();

                        schedule.setActualDurationMinutes(
                                Math.toIntExact(durationMinutes));

                        schedule.setStatus("COMPLETED");

                        if (!Boolean.TRUE.equals(
                                schedule.getTopicCompleted())
                                && (schedule.getEarlyClosureReason() == null
                                || schedule.getEarlyClosureReason().isBlank())) {

                            schedule.setEarlyClosureReason(
                                    "Google Meet session ended; "
                                    + "topic completion needs confirmation.");
                        }

                        scheduleRepository.save(schedule);
                    }
                }
            }
        }

        return repository.save(
                new MeetTrackingEvent(
                        meetingCode, eventType, source, now));
    }

    private TrainingSchedule findMatchingSchedule(
            String meetingCode,
            LocalDateTime now) {

        LocalDate today = now.toLocalDate();

        List<TrainingSchedule> candidates = new ArrayList<>();

        candidates.addAll(
                scheduleRepository
                        .findByMeetingCodeAndSessionDateAndStatusOrderByScheduledStartTimeAsc(
                                meetingCode, today, "SCHEDULED"));

        candidates.addAll(
                scheduleRepository
                        .findByMeetingCodeAndSessionDateAndStatusOrderByScheduledStartTimeAsc(
                                meetingCode, today, "IN_PROGRESS"));

        return candidates.stream()
                .filter(schedule -> schedule.getActualStartTime() == null)
                .filter(schedule -> schedule.getSessionDate() != null
                        && schedule.getSessionDate().equals(today))
                .filter(schedule -> schedule.getScheduledStartTime() != null)
                .filter(schedule -> schedule.getScheduledEndTime() != null)
                .filter(schedule -> {
                    LocalDateTime scheduledStart = LocalDateTime.of(
                            schedule.getSessionDate(),
                            schedule.getScheduledStartTime());

                    LocalDateTime scheduledEnd = LocalDateTime.of(
                            schedule.getSessionDate(),
                            schedule.getScheduledEndTime());

                    // Allow joining 30 minutes before the scheduled start
                    // and until the scheduled end time.
                    return !now.isBefore(scheduledStart.minusMinutes(30))
                            && !now.isAfter(scheduledEnd);
                })
                .min(Comparator.comparing(
                        TrainingSchedule::getScheduledStartTime))
                .orElse(null);
    }

    @GetMapping("/events")
    public List<MeetTrackingEvent> getAllEvents() {
        return repository.findAll();
    }

    @GetMapping("/events/{meetingCode}")
    public List<MeetTrackingEvent> getEventsByMeeting(
            @PathVariable("meetingCode") String meetingCode) {

        if (meetingCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Meeting code is required");
        }

        return repository.findByMeetingCodeOrderByDetectedAtDesc(
                meetingCode);
    }

    @GetMapping("/sessions/{meetingCode}")
    public List<MeetTrackingSession> getMeetingSessions(
            @PathVariable("meetingCode") String meetingCode) {

        if (meetingCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Meeting code is required");
        }

        return sessionRepository
                .findByMeetingCodeOrderByJoinTimeDesc(meetingCode);
    }

    @GetMapping("/sessions")
    public List<MeetTrackingSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    @GetMapping("/duration/{meetingCode}")
    public Map<String, Object> getMeetingDuration(
            @PathVariable("meetingCode") String meetingCode) {

        if (meetingCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Meeting code is required");
        }

        MeetTrackingSession session = sessionRepository
                .findFirstByMeetingCodeAndStatusOrderByJoinTimeDesc(
                        meetingCode, "COMPLETED")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Completed meeting session not found"));

        LocalDateTime joinTime = session.getJoinTime();
        LocalDateTime leaveTime = session.getLeaveTime();

        if (joinTime == null || leaveTime == null
                || leaveTime.isBefore(joinTime)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid meeting timestamps");
        }

        long durationSeconds =
                Duration.between(joinTime, leaveTime).getSeconds();

        return Map.of(
                "meetingCode", meetingCode,
                "sessionId", session.getId(),
                "trainingScheduleId",
                session.getTrainingScheduleId() == null
                        ? "UNMAPPED"
                        : session.getTrainingScheduleId(),
                "joinTime", joinTime,
                "leaveTime", leaveTime,
                "durationSeconds", durationSeconds,
                "durationMinutes", durationSeconds / 60
        );
    }
}
