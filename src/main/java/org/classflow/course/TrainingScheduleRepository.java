
package org.classflow.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingScheduleRepository
        extends JpaRepository<TrainingSchedule, Long> {

    List<TrainingSchedule> findByCourseIdOrderBySessionDateAsc(
            Long courseId);

    List<TrainingSchedule> findBySessionDateAndStatus(
            LocalDate sessionDate, String status);

    List<TrainingSchedule> findByStatusAndSessionDateBefore(
            String status, LocalDate sessionDate);

    Optional<TrainingSchedule> findByMeetingCode(String meetingCode);

    List<TrainingSchedule> findByMeetingCodeAndSessionDateAndStatusOrderByScheduledStartTimeAsc(
            String meetingCode,
            LocalDate sessionDate,
            String status);
}