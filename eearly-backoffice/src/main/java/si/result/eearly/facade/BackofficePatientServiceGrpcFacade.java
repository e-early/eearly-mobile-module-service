package si.result.eearly.facade;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.enums.Sex;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.ProfileLanguage;
import si.result.eearly.domain.profile.UpsertDemographicsCommand;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.genproto.UpsertPatientDemographicsRequest;
import si.result.eearly.genproto.UpsertPatientDemographicsResponse;
import si.result.eearly.service.ProfileService;

/**
 * Receives patient demographics from the admin service. The request carries personal data (date
 * of birth, sex): never log the request, the command or the profile, and keep the values out of
 * exception messages.
 */
@Service
@RequiredArgsConstructor
public class BackofficePatientServiceGrpcFacade {

  private final ProfileService profileService;

  @Transactional
  public UpsertPatientDemographicsResponse upsertPatientDemographics(
      UpsertPatientDemographicsRequest request) {
    try {
      profileService.upsertDemographics(toCommand(request));
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(),
          si.result.eearly.genproto.ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
    return UpsertPatientDemographicsResponse.newBuilder().build();
  }

  static UpsertDemographicsCommand toCommand(UpsertPatientDemographicsRequest request)
      throws ValidationException {
    try {
      UUID.fromString(request.getUserId());
    } catch (IllegalArgumentException e) {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, "userId must be a UUID");
    }

    LocalDate dateOfBirth = null;
    if (!request.getDateOfBirth().isEmpty()) {
      try {
        dateOfBirth = LocalDate.parse(request.getDateOfBirth());
      } catch (DateTimeParseException e) {
        // The cause is not chained: its message quotes the submitted text.
        throw new ValidationException(ErrorCode.VALIDATION_ERROR,
            "dateOfBirth must be an ISO-8601 date (yyyy-MM-dd)");
      }
    }

    Sex sex = switch (request.getSex()) {
      case MALE -> Sex.MALE;
      case FEMALE -> Sex.FEMALE;
      case OTHER -> Sex.OTHER;
      case UNKNOWN_SEX, UNRECOGNIZED -> null;
    };

    // Empty: an admin service that does not send the language, so keep the profile's language.
    String language = null;
    if (!request.getLanguage().isEmpty()) {
      if (!ProfileLanguage.isSupported(request.getLanguage())) {
        throw new ValidationException(ErrorCode.VALIDATION_ERROR,
            "language must be one of " + ProfileLanguage.SUPPORTED);
      }
      language = request.getLanguage();
    }

    return new UpsertDemographicsCommand(request.getUserId(), dateOfBirth, sex, language);
  }
}
