package si.result.eearly.domain.schedule;

import java.util.List;

public record UpsertScheduleCommand (
        String id,
        String name,
        String description,
        boolean active,
        String profileId,
        String caretakerId,
        List<ScheduleEntry> measurements
){
}
