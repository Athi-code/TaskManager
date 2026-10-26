
package org.classflow.course;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/training-schedules")
public class TrainingScheduleController {

    private final TrainingScheduleRepository scheduleRepository;
    private final CourseRepository courseRepository;

    public TrainingScheduleController(
            TrainingScheduleRepository scheduleRepository,
            CourseRepository courseRepository) {
        this.scheduleRepository = scheduleRepository;
        this.courseRepository = courseRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingSchedule createSchedule(
            @RequestBody TrainingSchedule schedule) {

        if (schedule.getCourseId() == null
                || !courseRepository.existsById(schedule.getCourseId())) {
            throw new IllegalArgumentException(
                    "Valid courseId is required.");
        }

        if (schedule.getSessionDate() == null
                || schedule.getScheduledStartTime() == null
                || schedule.getScheduledEndTime() == null) {
            throw new IllegalArgumentException(
                    "Session date, start time, and end time are required.");
        }

        if (!schedule.getScheduledEndTime()
                .isAfter(schedule.getScheduledStartTime())) {
            throw new IllegalArgumentException(
                    "Scheduled end time must be after start time.");
        }

        schedule.setStatus("SCHEDULED");
        schedule.setStartReminderSent(false);
        schedule.setFollowUpReminderSent(false);

        return scheduleRepository.save(schedule);
    }

    @GetMapping
    public List<TrainingSchedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @GetMapping("/{id}")
    public TrainingSchedule getScheduleById(
            @PathVariable("id") Long id) {
        return findSchedule(id);
    }

    @GetMapping("/course/{courseId}")
    public List<TrainingSchedule> getSchedulesByCourse(
            @PathVariable("courseId") Long courseId) {

        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("Course not found.");
        }

        return scheduleRepository
                .findByCourseIdOrderBySessionDateAsc(courseId);
    }

    
    @PostMapping("/{id}/start")
    public TrainingSchedule startClass(
            @PathVariable("id") Long id) {

        TrainingSchedule schedule = findSchedule(id);

        if (!"SCHEDULED".equals(schedule.getStatus())) {
            throw new IllegalStateException(
                    "Only a scheduled class can be started.");
        }

        if (schedule.getActualStartTime() != null) {
            throw new IllegalStateException(
                    "Class has already been started.");
        }

        LocalDateTime now = LocalDateTime.now();

        if (!schedule.getSessionDate().equals(now.toLocalDate())) {
            throw new IllegalStateException(
                    "Class can only be started on its scheduled date.");
        }

        schedule.setActualStartTime(now);
        schedule.setStatus("IN_PROGRESS");

        return scheduleRepository.save(schedule);
    }

   
    @PostMapping("/{id}/end")
    public TrainingSchedule endClass(
            @PathVariable("id") Long id,
            @RequestParam(name = "reason", required = false)
            String reason) {

        TrainingSchedule schedule = findSchedule(id);

        if (!"IN_PROGRESS".equals(schedule.getStatus())
                || schedule.getActualStartTime() == null) {
            throw new IllegalStateException(
                    "Start the class before ending it.");
        }

        LocalDateTime endTime = LocalDateTime.now();

        if (endTime.isBefore(schedule.getActualStartTime())) {
            throw new IllegalStateException(
                    "Class end time cannot be before start time.");
        }

        LocalDate actualStartDate =
                schedule.getActualStartTime().toLocalDate();

        if (!actualStartDate.equals(schedule.getSessionDate())) {
            throw new IllegalStateException(
                    "Class must start on its scheduled date.");
        }

        boolean topicCompleted =
                Boolean.TRUE.equals(schedule.getTopicCompleted());

        
        if (!topicCompleted
                && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException(
                    "Topic is incomplete. Provide a reason before ending the class.");
        }

        schedule.setActualEndTime(endTime);

        long durationMinutes = Duration.between(
                schedule.getActualStartTime(),
                endTime).toMinutes();

        schedule.setActualDurationMinutes(
                Math.toIntExact(durationMinutes));

        if (reason != null && !reason.isBlank()) {
            schedule.setEarlyClosureReason(reason);
        }

        schedule.setStatus("COMPLETED");

        return scheduleRepository.save(schedule);
    }

    
    @PutMapping("/{id}/topic")
    public TrainingSchedule updateTopic(
            @PathVariable("id") Long id,
            @RequestBody TopicUpdateRequest request) {

        TrainingSchedule schedule = findSchedule(id);

        if (request.getTopicName() == null
                || request.getTopicName().isBlank()) {
            throw new IllegalArgumentException(
                    "Topic name is required.");
        }

        schedule.setTopicName(request.getTopicName());
        schedule.setTopicCompleted(request.getTopicCompleted());
        schedule.setTopicNotes(request.getTopicNotes());

        return scheduleRepository.save(schedule);
    }

    private TrainingSchedule findSchedule(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Training schedule not found: " + id));
    }
 
    @PutMapping("/{id}/recover-topic")
    public TrainingSchedule recoverTopic(
            @PathVariable("id") Long id,
            @RequestBody TopicUpdateRequest request) {

        TrainingSchedule schedule = findSchedule(id);

        if (!"COMPLETED".equals(schedule.getStatus())) {
            throw new IllegalStateException(
                    "Only a completed class can have its pending topic recovered.");
        }

        if (schedule.getTopicName() == null
                || schedule.getTopicName().isBlank()) {
            throw new IllegalArgumentException(
                    "No topic is recorded for this class.");
        }

        if (request.getTopicCompleted() == null
                || !request.getTopicCompleted()) {
            throw new IllegalArgumentException(
                    "Topic must be marked as completed.");
        }

        schedule.setTopicCompleted(true);

        if (request.getTopicNotes() != null
                && !request.getTopicNotes().isBlank()) {
            schedule.setTopicNotes(request.getTopicNotes());
        }

        return scheduleRepository.save(schedule);
    }
}
