package si.result.eearly.domain.schedule;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
public class ScheduleUserId implements Serializable {

    private UUID userId;
    private UUID scheduleId;

    public ScheduleUserId() {}

    public ScheduleUserId(UUID userId, UUID scheduleId) {
        this.userId = userId;
        this.scheduleId = scheduleId;
    }
}
