package si.result.eearly.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import si.result.eearly.domain.enums.Sex;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.UpsertDemographicsCommand;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.genproto.UpsertPatientDemographicsRequest;
import si.result.eearly.service.ProfileService;

class BackofficePatientServiceGrpcFacadeTest {

  private static final String USER_ID = UUID.randomUUID().toString();

  private ProfileService profileService;
  private BackofficePatientServiceGrpcFacade facade;

  @BeforeEach
  void setUp() {
    profileService = mock(ProfileService.class);
    facade = new BackofficePatientServiceGrpcFacade(profileService);
  }

  @Test
  void passesDemographicsOnToTheProfileService() throws ValidationException {
    var response = facade.upsertPatientDemographics(request("1980-05-17",
        si.result.eearly.genproto.Sex.FEMALE));

    assertThat(response).isNotNull();
    ArgumentCaptor<UpsertDemographicsCommand> captor =
        ArgumentCaptor.forClass(UpsertDemographicsCommand.class);
    verify(profileService).upsertDemographics(captor.capture());
    assertThat(captor.getValue().userId()).isEqualTo(USER_ID);
    assertThat(captor.getValue().dateOfBirth()).isEqualTo(LocalDate.parse("1980-05-17"));
    assertThat(captor.getValue().sex()).isEqualTo(Sex.FEMALE);
  }

  @Test
  void mapsEverySexAndTreatsUnknownAsNotSet() throws ValidationException {
    assertThat(command("1980-05-17", si.result.eearly.genproto.Sex.MALE).sex()).isEqualTo(Sex.MALE);
    assertThat(command("1980-05-17", si.result.eearly.genproto.Sex.OTHER).sex()).isEqualTo(Sex.OTHER);
    assertThat(command("1980-05-17", si.result.eearly.genproto.Sex.UNKNOWN_SEX).sex()).isNull();
  }

  @Test
  void emptyDateOfBirthMeansNotSet() throws ValidationException {
    assertThat(command("", si.result.eearly.genproto.Sex.MALE).dateOfBirth()).isNull();
  }

  @Test
  void passesTheLanguageOn() throws ValidationException {
    var request = UpsertPatientDemographicsRequest.newBuilder()
        .setUserId(USER_ID).setDateOfBirth("1980-05-17")
        .setSex(si.result.eearly.genproto.Sex.MALE).setLanguage("en").build();

    facade.upsertPatientDemographics(request);

    ArgumentCaptor<UpsertDemographicsCommand> captor =
        ArgumentCaptor.forClass(UpsertDemographicsCommand.class);
    verify(profileService).upsertDemographics(captor.capture());
    assertThat(captor.getValue().language()).isEqualTo("en");
  }

  @Test
  void emptyLanguageMeansKeepTheCurrentOne() throws ValidationException {
    assertThat(command("1980-05-17", si.result.eearly.genproto.Sex.MALE).language()).isNull();
  }

  @Test
  void rejectsAnUnsupportedLanguage() throws ValidationException {
    var request = UpsertPatientDemographicsRequest.newBuilder()
        .setUserId(USER_ID).setDateOfBirth("1980-05-17")
        .setSex(si.result.eearly.genproto.Sex.MALE).setLanguage("de").build();

    var e = catchThrowableOfType(ServiceInvalidRequestException.class,
        () -> facade.upsertPatientDemographics(request));

    assertThat(e).isNotNull();
    assertThat(e.getErrorCode()).isEqualTo(si.result.eearly.genproto.ErrorCode.VALIDATION_ERROR);
    verify(profileService, never()).upsertDemographics(any());
  }

  @Test
  void rejectsUserIdThatIsNotAUuid() throws ValidationException {
    var request = UpsertPatientDemographicsRequest.newBuilder().setUserId("not-a-uuid").build();

    var e = catchThrowableOfType(ServiceInvalidRequestException.class,
        () -> facade.upsertPatientDemographics(request));

    assertThat(e).isNotNull();
    assertThat(e.getErrorCode()).isEqualTo(si.result.eearly.genproto.ErrorCode.VALIDATION_ERROR);
    verify(profileService, never()).upsertDemographics(any());
  }

  @Test
  void rejectsMalformedDateOfBirthWithoutEchoingIt() throws ValidationException {
    var request = request("17.05.1980", si.result.eearly.genproto.Sex.MALE);

    var e = catchThrowableOfType(ServiceInvalidRequestException.class,
        () -> facade.upsertPatientDemographics(request));

    assertThat(e).isNotNull();
    assertThat(e.getErrorCode()).isEqualTo(si.result.eearly.genproto.ErrorCode.VALIDATION_ERROR);
    assertThat(e.getMessage()).doesNotContain("1980");
    assertThat(e.getCause()).isNull();
    verify(profileService, never()).upsertDemographics(any());
  }

  @Test
  void mapsServiceValidationErrorsToInvalidRequest() throws ValidationException {
    when(profileService.upsertDemographics(any())).thenThrow(
        new ValidationException(ErrorCode.VALIDATION_ERROR, "dateOfBirth must be in the past"));
    var request = request("2999-01-01", si.result.eearly.genproto.Sex.MALE);

    var e = catchThrowableOfType(ServiceInvalidRequestException.class,
        () -> facade.upsertPatientDemographics(request));

    assertThat(e).isNotNull();
    assertThat(e.getErrorCode()).isEqualTo(si.result.eearly.genproto.ErrorCode.VALIDATION_ERROR);
    assertThat(e.getMessage()).isEqualTo("dateOfBirth must be in the past");
  }

  private static UpsertPatientDemographicsRequest request(String dateOfBirth,
      si.result.eearly.genproto.Sex sex) {
    return UpsertPatientDemographicsRequest.newBuilder()
        .setUserId(USER_ID).setDateOfBirth(dateOfBirth).setSex(sex).build();
  }

  private static UpsertDemographicsCommand command(String dateOfBirth,
      si.result.eearly.genproto.Sex sex) throws ValidationException {
    return BackofficePatientServiceGrpcFacade.toCommand(request(dateOfBirth, sex));
  }
}
