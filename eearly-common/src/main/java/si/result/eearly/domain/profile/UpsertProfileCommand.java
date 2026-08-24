package si.result.eearly.domain.profile;

public record UpsertProfileCommand(
        String userId,
        byte[] picture,
        boolean notificationDaySummary,
        String notificationDaySummaryTimeString,
        boolean notificationMeasurementDue,
        Integer notificationMeasurementDueMinutes,
        boolean notificationNoInternet,
        boolean notificationNoBluetooth
        ) {
}
