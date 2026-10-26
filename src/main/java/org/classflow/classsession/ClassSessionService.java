
package org.classflow.classsession;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassSessionService {

    private final ClassSessionRepository repository;

    public ClassSessionService(ClassSessionRepository repository) {
        this.repository = repository;
    }

    public ClassSession createSession(ClassSession session) {
        session.setStatus("SCHEDULED");
        session.setMissedConfirmed(false);
        session.setActualStartTime(null);
        session.setRecoveryStatus("PENDING");
        session.setRecoveryDateTime(null);
        session.setRecoveryNotes(null);

        return repository.save(session);
    }

    public List<ClassSession> getAllSessions() {
        return repository.findAll();
    }

    public ClassSession getSessionById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Class session not found: " + id));
    }

    @Transactional
    public ClassSession recordStart(Long id) {
        ClassSession session = getSessionById(id);

        session.setActualStartTime(LocalDateTime.now());
        session.setStatus("START_DETECTED");
        session.setMissedConfirmed(false);

        return repository.save(session);
    }

    @Transactional
    public ClassSession confirmMissed(Long id) {
        ClassSession session = getSessionById(id);

        if ("START_DETECTED".equals(session.getStatus())) {
            throw new IllegalStateException(
                    "This session already has a recorded start event.");
        }

        session.setStatus("MISSED_CONFIRMED");
        session.setMissedConfirmed(true);

        return repository.save(session);
    }

    public String getDelayImpact(Long id) {
        ClassSession session = getSessionById(id);

        if (!session.isMissedConfirmed()) {
            return "Session is not confirmed as missed. "
                    + "Please verify before planning recovery.";
        }

        String message = "Missed session: " + session.getTopic()
                + ". Scheduled start: " + session.getScheduledStart() + ".";

        if (session.getRecoveryDateTime() == null) {
            return message
                    + " Recovery has not been scheduled yet. "
                    + "A catch-up session may be required.";
        }

        if (session.getScheduledStart() == null) {
            return message
                    + " Recovery is scheduled for "
                    + session.getRecoveryDateTime()
                    + ". Cannot calculate the recovery gap because "
                    + "the original scheduled start is missing.";
        }

        Duration gap = Duration.between(
                session.getScheduledStart(),
                session.getRecoveryDateTime());

        long totalMinutes = gap.toMinutes();
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        return message
                + " Recovery is scheduled for "
                + session.getRecoveryDateTime()
                + ". Recovery gap from the original scheduled start: "
                + days + " day(s), "
                + hours + " hour(s), and "
                + minutes + " minute(s)."
                + " This is the recovery gap, not a confirmed course "
                + "completion delay.";
    }

    @Transactional
    public ClassSession scheduleRecovery(
            Long id, RecoveryRequest request) {

        ClassSession session = getSessionById(id);

        if (!session.isMissedConfirmed()) {
            throw new IllegalStateException(
                    "Confirm the missed session before scheduling recovery.");
        }

        if (request == null || request.getRecoveryDateTime() == null) {
            throw new IllegalArgumentException(
                    "Recovery date and time are required.");
        }

        LocalDateTime recoveryDateTime = request.getRecoveryDateTime();

        if (!recoveryDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Recovery date and time must be in the future.");
        }

        if (session.getScheduledStart() != null
                && !recoveryDateTime.isAfter(session.getScheduledStart())) {
            throw new IllegalArgumentException(
                    "Recovery date and time must be after "
                            + "the original scheduled start.");
        }

        session.setRecoveryDateTime(recoveryDateTime);
        session.setRecoveryNotes(request.getRecoveryNotes());
        session.setRecoveryStatus("SCHEDULED");

        return repository.save(session);
    }

    public ClassSession getRecoveryDetails(Long id) {
        return getSessionById(id);
    }

    public String getDependencyImpact(Long id) {

        ClassSession currentSession = getSessionById(id);

        Long prerequisiteId = currentSession.getDependsOnSessionId();

        if (prerequisiteId == null) {
            return "No prerequisite session is configured for: "
                    + currentSession.getTopic();
        }

        if (prerequisiteId.equals(id)) {
            throw new IllegalArgumentException(
                    "A session cannot depend on itself.");
        }

        ClassSession prerequisite = getSessionById(prerequisiteId);

        String message = "Session: " + currentSession.getTopic()
                + " depends on: " + prerequisite.getTopic() + ". ";

        if (!prerequisite.isMissedConfirmed()) {
            return message
                    + "The prerequisite is not confirmed as missed. "
                    + "No missed-session dependency risk is identified.";
        }

        if (prerequisite.getRecoveryDateTime() == null) {
            return message
                    + "The prerequisite session was missed and recovery "
                    + "is not scheduled. The dependent session may be at risk.";
        }

        if (currentSession.getScheduledStart() == null) {
            return message
                    + "Recovery is scheduled for "
                    + prerequisite.getRecoveryDateTime()
                    + ", but the dependent session has no scheduled start "
                    + "time, so schedule impact cannot be assessed.";
        }

        if (prerequisite.getRecoveryDateTime()
                .isAfter(currentSession.getScheduledStart())) {

            return message
                    + "Recovery is scheduled for "
                    + prerequisite.getRecoveryDateTime()
                    + ", after the dependent session's scheduled start at "
                    + currentSession.getScheduledStart()
                    + ". The dependent session may be affected.";
        }

        return message
                + "Recovery is scheduled for "
                + prerequisite.getRecoveryDateTime()
                + ", before or at the dependent session's scheduled start. "
                + "No schedule conflict is identified by this check.";
    }

    @Transactional
    public ClassSession setDependency(
            Long sessionId, Long prerequisiteId) {

        ClassSession session = getSessionById(sessionId);

        if (prerequisiteId == null) {
            session.setDependsOnSessionId(null);
            return repository.save(session);
        }

        if (sessionId.equals(prerequisiteId)) {
            throw new IllegalArgumentException(
                    "A session cannot depend on itself.");
        }

        ClassSession prerequisite = getSessionById(prerequisiteId);

       
        Long nextId = prerequisite.getDependsOnSessionId();

        while (nextId != null) {

            if (nextId.equals(sessionId)) {
                throw new IllegalArgumentException(
                        "Circular dependency detected. "
                                + "This dependency would create a loop.");
            }

            ClassSession nextSession = getSessionById(nextId);
            nextId = nextSession.getDependsOnSessionId();
        }

        session.setDependsOnSessionId(prerequisiteId);

        return repository.save(session);
    }
}
