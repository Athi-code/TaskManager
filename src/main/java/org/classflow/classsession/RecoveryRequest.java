
package org.classflow.classsession;

import java.time.LocalDateTime;

public class RecoveryRequest {

    private LocalDateTime recoveryDateTime;
    private String recoveryNotes;

    public RecoveryRequest() {
    }

    public LocalDateTime getRecoveryDateTime() {
        return recoveryDateTime;
    }

    public void setRecoveryDateTime(LocalDateTime recoveryDateTime) {
        this.recoveryDateTime = recoveryDateTime;
    }

    public String getRecoveryNotes() {
        return recoveryNotes;
    }

    public void setRecoveryNotes(String recoveryNotes) {
        this.recoveryNotes = recoveryNotes;
    }
}
