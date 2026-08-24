package si.result.eearly.service.firebase;

import si.result.eearly.domain.schedule.Schedule;

public interface FirebaseService {

  String sendNotificationForToken(String token, String title, String body);

  void sendNewSchedule(Schedule schedule);

  void sendMeasurementReminder(Schedule schedule, String scheduledAt, String scheduleName);
}
