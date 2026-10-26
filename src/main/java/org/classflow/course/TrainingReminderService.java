package org.classflow.course;

import org.classflow.notification.DesktopReminderNotifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class TrainingReminderService {

    private final TrainingScheduleRepository scheduleRepository;

    public TrainingReminderService(TrainingScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void checkTrainingReminders() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        List<TrainingSchedule> schedules =
                scheduleRepository.findBySessionDateAndStatus(today, "SCHEDULED");

        for (TrainingSchedule schedule : schedules) {
            processReminder(schedule, now);
        }

        detectMissedClasses(today);
    }

    private void processReminder(TrainingSchedule schedule, LocalTime now) {
        LocalTime startTime = schedule.getScheduledStartTime();
        if (startTime == null) {
            return;
        }

        if (!schedule.isStartReminderSent()
                && !now.isBefore(startTime.minusMinutes(10))) {
            showReminder(
                    "CLASSFLOW - Class starts in 10 minutes",
                    "Your training class is scheduled for " + startTime + ". Please get ready.",
                    schedule.getId());
            schedule.setStartReminderSent(true);
            scheduleRepository.save(schedule);
        }

        if (now.equals(startTime) && schedule.getActualStartTime() == null) {
            showReminder(
                    "CLASSFLOW - Class start check",
                    "Your training class is scheduled to start now.",
                    schedule.getId());
        }

        if (!schedule.isFollowUpReminderSent()
                && schedule.getActualStartTime() == null
                && !now.isBefore(startTime.plusMinutes(10))) {
            showReminder(
                    "CLASSFLOW - Follow-up reminder",
                    "The class has not been marked as started. Please check.",
                    schedule.getId());
            schedule.setFollowUpReminderSent(true);
            scheduleRepository.save(schedule);
        }
    }

    private void detectMissedClasses(LocalDate today) {
        List<TrainingSchedule> missedCandidates =
                scheduleRepository.findByStatusAndSessionDateBefore("SCHEDULED", today);

        for (TrainingSchedule schedule : missedCandidates) {
            schedule.setStatus("MISSED");
            scheduleRepository.save(schedule);
            showReminder(
                    "CLASSFLOW - Missed class",
                    "The scheduled class date has passed without being started.",
                    schedule.getId());
        }
    }

    public String testReminder(Long scheduleId) {
        TrainingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException(
                        "Training schedule not found: " + scheduleId));

        showReminder(
                "CLASSFLOW - Test reminder",
                "This is a test reminder for your training class.",
                schedule.getId());

        return "Test reminder requested for schedule ID: " + schedule.getId()
                + ". Check your Windows desktop and Eclipse Console.";
    }

    private void showReminder(String title, String message, Long scheduleId) {
        String fullMessage = "Schedule ID: " + scheduleId + " | " + message;

        System.out.println();
        System.out.println("======================================");
        System.out.println(title);
        System.out.println(fullMessage);
        System.out.println("Reminder time: " + LocalTime.now());
        System.out.println("======================================");
        System.out.println();

        DesktopReminderNotifier.show(title, fullMessage);
    }
}
