package si.result.eearly.service.firebase;


import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import si.result.eearly.domain.enums.FirebaseMessageType;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.domain.token.MobileToken;
import si.result.eearly.exception.FirebaseNotificationException;
import si.result.eearly.repository.MobileTokenRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseServiceImpl implements FirebaseService {

  private final FirebaseMessaging fcm;
  private final MobileTokenRepository mobileTokenRepository;

  @Override
  public String sendNotificationForToken(String token, String title, String body) {

    Message message = Message.builder()
        .setToken(token)
        .setNotification(Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build())
        .build();


    String returnMessage;
    try {
      returnMessage = fcm.send(message);
      log.info("Sent notification to token with response: " + returnMessage);
      return returnMessage;
    } catch (FirebaseMessagingException e) {
      log.error("Failed to send notification to token", e);
      throw new FirebaseNotificationException(e.getMessage());
    }
  }

  @Override
  @Transactional
  public void sendNewSchedule(Schedule schedule) {
    List<String> mobileTokens = schedule.getProfiles().stream()
        .flatMap(profile -> profile.getMobileTokens().stream())
        .map(MobileToken::getToken)
        .toList();

    if (mobileTokens.isEmpty()) {
      log.info("No mobile tokens found for schedule {}, skipping Firebase notification", schedule.getId());
      return;
    }

    String titleLocKey = "new_schedule_title";
    String bodyLocKey = "new_schedule_subtitle";

    // https://firebase.google.com/docs/reference/admin/java/reference/com/google/firebase/messaging/MulticastMessage
    // Up to 500 tokens
    // TODO: How to increase number of tokens?
    MulticastMessage message = MulticastMessage.builder()
        .putData("type", FirebaseMessageType.NEW_SCHEDULE.toString())
        .putData("scheduleId", schedule.getId().toString())
        .setAndroidConfig(AndroidConfig.builder()
            .setNotification(AndroidNotification.builder()
                .setTitleLocalizationKey(titleLocKey)
                .setBodyLocalizationKey(bodyLocKey)
                .build())
            .build())
        .setApnsConfig(ApnsConfig.builder()
            .setAps(Aps.builder()
                .setAlert(ApsAlert.builder()
                    .setTitleLocalizationKey(titleLocKey)
                    .setSubtitleLocalizationKey(bodyLocKey)
                    .build())
                .build())
            .build())
        .addAllTokens(mobileTokens)
        .build();

    try {
      BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(message);

      log.debug("Successful responses: {}", batchResponse.getSuccessCount());
      log.debug("Failed responses: {}", batchResponse.getFailureCount());

      for (int i = 0; i < mobileTokens.size(); i++) {
        SendResponse response = batchResponse.getResponses().get(i);
        String mobileToken = mobileTokens.get(i);

        if (response.isSuccessful()) {
          continue;
        }

        FirebaseMessagingException exception = response.getException();

        if (exception.getErrorCode() == ErrorCode.NOT_FOUND
            && exception.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
          log.warn("Token is not registered anymore, deleting it from database: {}", mobileToken);
          mobileTokenRepository.deleteByToken(mobileToken);
        } else {
          log.error("Failed to send notification for token: {}", mobileTokens, response.getException());
        }
      }
    } catch (FirebaseMessagingException e) {
      log.error("Failed to send Multicast Message", e);
      throw new FirebaseNotificationException(e.getMessage());
    }
  }

  @Override
  @Transactional
  public void sendMeasurementReminder(Schedule schedule, String scheduledAt, String scheduleName) {
    if (schedule.getProfiles() == null || schedule.getProfiles().isEmpty()) {
      throw new FirebaseNotificationException(
          "No profiles linked to schedule " + schedule.getId() + " for measurement reminder");
    }

    List<String> mobileTokens = schedule.getProfiles().stream()
        .filter(Profile::isNotificationMeasurementDue)
        .flatMap(profile -> profile.getMobileTokens().stream())
        .map(MobileToken::getToken)
        .toList();

    if (mobileTokens.isEmpty()) {
      throw new FirebaseNotificationException(
          "No mobile tokens with measurement reminders enabled for schedule " + schedule.getId());
    }

    String titleLocKey = "measurement_reminder_title";
    String bodyLocKey = "measurement_reminder_subtitle";

    MulticastMessage message = MulticastMessage.builder()
        .putData("type", FirebaseMessageType.MEASUREMENT_REMINDER.toString())
        .putData("scheduleId", schedule.getId().toString())
        .putData("scheduledAt", scheduledAt)
        .putData("scheduleName", scheduleName != null ? scheduleName : "")
        .setAndroidConfig(AndroidConfig.builder()
            .setNotification(AndroidNotification.builder()
                .setTitleLocalizationKey(titleLocKey)
                .setBodyLocalizationKey(bodyLocKey)
                .build())
            .build())
        .setApnsConfig(ApnsConfig.builder()
            .setAps(Aps.builder()
                .setAlert(ApsAlert.builder()
                    .setTitleLocalizationKey(titleLocKey)
                    .setSubtitleLocalizationKey(bodyLocKey)
                    .build())
                .build())
            .build())
        .addAllTokens(mobileTokens)
        .build();

    try {
      BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(message);
      log.info("Measurement reminder for schedule {}: {} success, {} failed",
          schedule.getId(), batchResponse.getSuccessCount(), batchResponse.getFailureCount());

      for (int i = 0; i < mobileTokens.size(); i++) {
        SendResponse response = batchResponse.getResponses().get(i);
        if (response.isSuccessful()) {
          continue;
        }
        FirebaseMessagingException exception = response.getException();
        String mobileToken = mobileTokens.get(i);
        if (exception != null
            && exception.getErrorCode() == ErrorCode.NOT_FOUND
            && exception.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
          log.warn("Token is not registered anymore, deleting it from database: {}", mobileToken);
          mobileTokenRepository.deleteByToken(mobileToken);
        } else {
          log.error("Failed to send measurement reminder for token {}", mobileToken, response.getException());
        }
      }

      if (batchResponse.getFailureCount() > 0 && batchResponse.getSuccessCount() == 0) {
        throw new FirebaseNotificationException("All measurement reminder pushes failed for schedule " + schedule.getId());
      }
    } catch (FirebaseMessagingException e) {
      log.error("Failed to send measurement reminder multicast", e);
      throw new FirebaseNotificationException(e.getMessage());
    }
  }
}
