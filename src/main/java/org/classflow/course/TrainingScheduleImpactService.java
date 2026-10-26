
package org.classflow.course;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class TrainingScheduleImpactService {

    private final TrainingScheduleRepository scheduleRepository;

    public TrainingScheduleImpactService(
            TrainingScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public Map<String, Object> analyzeCourseImpact(Long courseId) {

        Map<String, Object> report = new HashMap<>();

        List<TrainingSchedule> schedules =
                scheduleRepository.findByCourseIdOrderBySessionDateAsc(courseId);

        List<TrainingSchedule> incompleteTopics = new ArrayList<>();
        List<TrainingSchedule> unconfirmedTopics = new ArrayList<>();
        List<TrainingSchedule> upcomingClasses = new ArrayList<>();
        List<TrainingSchedule> missedClasses = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (TrainingSchedule schedule : schedules) {

            boolean classEnded =
                    "COMPLETED".equalsIgnoreCase(schedule.getStatus());

            boolean classScheduled =
                    "SCHEDULED".equalsIgnoreCase(schedule.getStatus());

            
            if (classEnded
                    && Boolean.FALSE.equals(schedule.getTopicCompleted())) {

                incompleteTopics.add(schedule);
            }

          
            if (classEnded
                    && schedule.getTopicCompleted() == null) {

                unconfirmedTopics.add(schedule);
            }

            
            if (classScheduled
                    && schedule.getSessionDate() != null
                    && schedule.getSessionDate().isBefore(today)) {

                missedClasses.add(schedule);
            }

            
            if (classScheduled
                    && schedule.getSessionDate() != null
                    && !schedule.getSessionDate().isBefore(today)) {

                upcomingClasses.add(schedule);
            }
        }

        report.put("courseId", courseId);
        report.put("totalClasses", schedules.size());

        report.put("incompleteTopicCount", incompleteTopics.size());
        report.put("incompleteTopics", incompleteTopics);

        report.put("unconfirmedTopicCount", unconfirmedTopics.size());
        report.put("unconfirmedTopics", unconfirmedTopics);

        report.put("missedClassCount", missedClasses.size());
        report.put("missedClasses", missedClasses);

        report.put("upcomingClassCount", upcomingClasses.size());
        report.put("upcomingClasses", upcomingClasses);

        
        if (!incompleteTopics.isEmpty() || !missedClasses.isEmpty()) {

            report.put(
                    "impactMessage",
                    "Attention: Review missed classes and incomplete topics."
            );

            report.put(
                    "recoverySuggestion",
                    "Review incomplete topics and plan recovery sessions before continuing dependent training topics."
            );

        } else if (!unconfirmedTopics.isEmpty()) {

            report.put(
                    "impactMessage", "Some completed classes have unconfirmed topic completion."
            );

            report.put(
                    "recoverySuggestion","Confirm whether the topics were completed before planning recovery sessions."
            );

        } else {

            report.put(
                    "impactMessage","No missed classes or incomplete topics detected."
            );

            report.put(
                    "recoverySuggestion","Continue with the scheduled training plan."
            );
        }

        return report;
    }
}
