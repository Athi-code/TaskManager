
package org.classflow.meet;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetTrackingSessionRepository
        extends JpaRepository<MeetTrackingSession, Long> {

    List<MeetTrackingSession>
            findByMeetingCodeOrderByJoinTimeDesc(String meetingCode);

    Optional<MeetTrackingSession>
            findFirstByMeetingCodeAndStatusOrderByJoinTimeDesc(
                    String meetingCode, String status);
}
