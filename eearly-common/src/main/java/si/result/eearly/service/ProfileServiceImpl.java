package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.domain.profile.UpsertProfileCommand;
import si.result.eearly.ehr.EhrbaseClient;
import si.result.eearly.repository.ProfileRepository;
import si.result.eearly.service.firebase.FirebaseService;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

  private final ProfileRepository profileRepository;
  private final FirebaseService firebaseService;
  private final EhrbaseClient ehrbaseClient;

  @Override
  public Profile upsertProfile(UpsertProfileCommand command) throws ValidationException {
    var userId = UUID.fromString(command.userId());

    if(!ehrbaseClient.doesEhrExist(userId)) {
      ehrbaseClient.createEhr(userId);
    }

    validateProfileUpsert(command);

    var picture = new byte[0];
    Optional<Profile> existingProfile = profileRepository.findById(UUID.fromString(command.userId()));

    if (existingProfile.isPresent() && command.picture().length == 0) {
      picture = existingProfile.get().getPicture();
    } else {
      picture = command.picture();
    }

    final var profileToUpsert = Profile.createProfileFromUpsertCommand(picture, command);

    if (existingProfile.isPresent()) {
      existingProfile.get().apply(profileToUpsert);
      profileRepository.save(existingProfile.get());
    } else {
      profileRepository.save(profileToUpsert);
    }

    return profileToUpsert;
  }

  @Override
  public String sendNotificationForToken(String token, String title, String body) {

    return firebaseService.sendNotificationForToken(token, title, body);
  }

  @Override
  public Profile getProfile(String id) throws ValidationException{
    return profileRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ValidationException(
            ErrorCode.INTERNAL_ERROR, String.format("User profile with id %s does not exist", id)));
  }

  void validateProfileUpsert(UpsertProfileCommand command) throws ValidationException {
    validateNotificationDaySummary(command);
    validateNotificationMeasurementDue(command);
  }

  void validateNotificationDaySummary(UpsertProfileCommand command) throws ValidationException {
    if (command.notificationDaySummary()) {
      if (command.notificationDaySummaryTimeString() == null) {
        throw new ValidationException(ErrorCode.VALIDATION_ERROR, "notificationDaySummaryTime must be set when notificationDaySummary is TRUE");
      }
      try {
        LocalTime.parse(command.notificationDaySummaryTimeString());
      } catch (DateTimeParseException e) {
        throw new ValidationException(ErrorCode.VALIDATION_ERROR, "notificationDaySummaryTime is not a valid time (HH:mm:ss)");
      }
    }
  }

  void validateNotificationMeasurementDue(UpsertProfileCommand command) throws ValidationException {
    if (command.notificationMeasurementDue() && command.notificationMeasurementDueMinutes() == 0) {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, "notificationMeasurementDueMinutes must be set when notificationMeasurementDue is TRUE");
    }
  }
}
