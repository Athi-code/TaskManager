
package org.classflow.course;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/training-reminders")
public class TrainingReminderController {

    private final TrainingReminderService reminderService;

    public TrainingReminderController(
            TrainingReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping("/test/{scheduleId}")
    @ResponseStatus(HttpStatus.OK)
    public String testReminder(
            @PathVariable("scheduleId") Long scheduleId) {
        return reminderService.testReminder(scheduleId);
    }
}
