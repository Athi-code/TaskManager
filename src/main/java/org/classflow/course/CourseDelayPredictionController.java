
package org.classflow.course;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/course-delay")
public class CourseDelayPredictionController {

    private final CourseDelayPredictionService predictionService;

    public CourseDelayPredictionController(
            CourseDelayPredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/{courseId}")
    public Map<String, Object> predictDelay(
            @PathVariable("courseId") Long courseId) {
        return predictionService.predictCourseDelay(courseId);
    }
}
