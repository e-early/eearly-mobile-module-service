package si.result.eearly.domain.schedule;

public record CreateUpdateScheduleEntryCommand(
        String id,
        String measurementTypeId,
        String scheduledAt,
        String scheduledMode,
        String measurementMode,
        String description,
        int measurementsInBatch,
        int intervalMinutes,
        String scheduleId
) {
}
