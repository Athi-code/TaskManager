
package org.classflow.meet;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MeetTrackingRepository
        extends JpaRepository<MeetTrackingEvent, Long> {

    List<MeetTrackingEvent> findByMeetingCodeOrderByDetectedAtDesc(
            String meetingCode);

    Optional<MeetTrackingEvent>
            findFirstByMeetingCodeAndEventTypeOrderByDetectedAtAsc(
                    String meetingCode, String eventType);

    Optional<MeetTrackingEvent>
            findFirstByMeetingCodeAndEventTypeOrderByDetectedAtDesc(
                    String meetingCode, String eventType);
}
