
package org.classflow.course;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseDelayPredictionService {

    private final CourseRepository courseRepository;
    private final TrainingScheduleRepository scheduleRepository;

    public CourseDelayPredictionService(
            CourseRepository courseRepository,
            TrainingScheduleRepository scheduleRepository) {
        this.courseRepository = courseRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public Map<String, Object> predictCourseDelay(Long courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->new RuntimeException("Course not found: " + courseId));

        List<TrainingSchedule> schedules =scheduleRepository.findByCourseIdOrderBySessionDateAsc(courseId);

        int completedSessions = 0;
        int inProgressSessions = 0;
        int scheduledSessions = 0;
        int missedSessions = 0;
        int pendingTopicSessions = 0;

        List<Map<String, Object>> pendingTopics = new ArrayList<>();

        for (TrainingSchedule schedule : schedules) {

            String status = schedule.getStatus();

            if ("COMPLETED".equals(status)) {
                completedSessions++;

               
                if (Boolean.FALSE.equals(schedule.getTopicCompleted())) {
                    pendingTopicSessions++;

                    Map<String, Object> topicDetails = new HashMap<>();
                    topicDetails.put("scheduleId", schedule.getId());
                    topicDetails.put("sessionDate", schedule.getSessionDate());
                    topicDetails.put("topicName", schedule.getTopicName());
                    topicDetails.put("topicNotes", schedule.getTopicNotes());

                    pendingTopics.add(topicDetails);
                }

            } else if ("IN_PROGRESS".equals(status)) {
                inProgressSessions++;

            } else if ("SCHEDULED".equals(status)) {
                scheduledSessions++;

            } else if ("MISSED".equals(status)) {
                missedSessions++;
            }
        }

        int totalPlannedSessions =
                course.getTotalPlannedSessions();

        int remainingSessions = Math.max(
                0, totalPlannedSessions - completedSessions);

        LocalDate plannedEndDate = course.getEndDate();
        LocalDate estimatedCompletionDate = plannedEndDate;

        
        long estimatedDelayDays =
                (long) missedSessions + pendingTopicSessions;

        if (plannedEndDate != null && estimatedDelayDays > 0) {
            estimatedCompletionDate =
                    plannedEndDate.plusDays(estimatedDelayDays);
        }

        String predictionStatus =
                estimatedDelayDays > 0
                        ? "POTENTIAL_DELAY"
                        : "ON_TRACK";

        Map<String, Object> result = new HashMap<>();

        result.put("courseId", course.getId());
        result.put("courseName", course.getCourseName());
        result.put("plannedStartDate", course.getStartDate());
        result.put("plannedEndDate", plannedEndDate);

        result.put("totalPlannedSessions", totalPlannedSessions);
        result.put("completedSessions", completedSessions);
        result.put("inProgressSessions", inProgressSessions);
        result.put("scheduledSessions", scheduledSessions);
        result.put("missedSessions", missedSessions);
        result.put("remainingSessions", remainingSessions);

        result.put("pendingTopicSessions", pendingTopicSessions);
        result.put("pendingTopics", pendingTopics);

        result.put("estimatedCompletionDate",
                estimatedCompletionDate);
        result.put("estimatedDelayDays", estimatedDelayDays);
        result.put("predictionStatus", predictionStatus);

        result.put("note",
                "Initial estimate only. Each missed session and each completed session with a pending topic adds one calendar day. Makeup classes, holidays, topic recovery, and available training days are not yet considered.");

        return result;
    }
}
