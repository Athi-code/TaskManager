
package org.classflow.course;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/training-schedules")
public class TrainingScheduleImpactController {

    private final TrainingScheduleRepository scheduleRepository;
    private final TrainingScheduleImpactService impactService;

    public TrainingScheduleImpactController(
            TrainingScheduleRepository scheduleRepository,
            TrainingScheduleImpactService impactService) {

        this.scheduleRepository = scheduleRepository;
        this.impactService = impactService;
    }

    @GetMapping("/course/{courseId}/impact")
    public Map<String, Object> getCourseImpact(
            @PathVariable("courseId") Long courseId) {

        boolean courseExists = scheduleRepository.findAll()
                .stream()
                .anyMatch(schedule ->
                        courseId.equals(schedule.getCourseId()));

        if (!courseExists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No training schedules found for course: " + courseId
            );
        }

        return impactService.analyzeCourseImpact(courseId);
    }
}
