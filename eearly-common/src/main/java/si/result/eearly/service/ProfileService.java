package si.result.eearly.service;

import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.domain.profile.UpsertProfileCommand;

public interface ProfileService {

  Profile upsertProfile(UpsertProfileCommand command) throws ValidationException;
  String sendNotificationForToken(String token, String title, String body);
  Profile getProfile(String id) throws ValidationException;
}
