package si.result.eearly.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import si.result.eearly.admin.AdminServiceClient;
import si.result.eearly.domain.enums.Sex;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.domain.profile.UpsertDemographicsCommand;
import si.result.eearly.ehr.EhrbaseClient;
import si.result.eearly.repository.ProfileRepository;
import si.result.eearly.service.push.PushNotificationService;

class ProfileServiceDemographicsTest {

  private static final UUID USER_ID = UUID.randomUUID();
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneOffset.UTC);

  private ProfileRepository profileRepository;
  private ProfileServiceImpl profileService;

  @BeforeEach
  void setUp() {
    profileRepository = mock(ProfileRepository.class);
    when(profileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    profileService = new ProfileServiceImpl(profileRepository, mock(EhrbaseClient.class), CLOCK,
        mock(AdminServiceClient.class), mock(PushNotificationService.class));
  }

  @Test
  void createsProfileForPatientWhoHasNotOpenedTheAppYet() throws ValidationException {
    when(profileRepository.findById(USER_ID)).thenReturn(Optional.empty());

    profileService.upsertDemographics(new UpsertDemographicsCommand(
        USER_ID.toString(), LocalDate.parse("1980-05-17"), Sex.FEMALE, "en"));

    Profile saved = savedProfile();
    assertThat(saved.getUserId()).isEqualTo(USER_ID);
    assertThat(saved.getDateOfBirth()).isEqualTo(LocalDate.parse("1980-05-17"));
    assertThat(saved.getSex()).isEqualTo(Sex.FEMALE);
    assertThat(saved.getLanguage()).isEqualTo("en");
  }

  @Test
  void updatesDemographicsWithoutTouchingAppSettings() throws ValidationException {
    Profile existing = Profile.createProfileFromId(USER_ID.toString());
    existing.setNotificationDaySummary(true);
    existing.setNotificationDaySummaryTime(LocalTime.of(20, 0));
    existing.setSex(Sex.MALE);
    when(profileRepository.findById(USER_ID)).thenReturn(Optional.of(existing));

    profileService.upsertDemographics(new UpsertDemographicsCommand(
        USER_ID.toString(), LocalDate.parse("1980-05-17"), Sex.OTHER, "sl"));

    Profile saved = savedProfile();
    assertThat(saved.getSex()).isEqualTo(Sex.OTHER);
    assertThat(saved.getDateOfBirth()).isEqualTo(LocalDate.parse("1980-05-17"));
    assertThat(saved.isNotificationDaySummary()).isTrue();
    assertThat(saved.getNotificationDaySummaryTime()).isEqualTo(LocalTime.of(20, 0));
  }

  @Test
  void missingValuesClearTheStoredOnes() throws ValidationException {
    Profile existing = Profile.createProfileFromId(USER_ID.toString());
    existing.setDateOfBirth(LocalDate.parse("1980-05-17"));
    existing.setSex(Sex.MALE);
    when(profileRepository.findById(USER_ID)).thenReturn(Optional.of(existing));

    profileService.upsertDemographics(new UpsertDemographicsCommand(USER_ID.toString(), null, null, null));

    Profile saved = savedProfile();
    assertThat(saved.getDateOfBirth()).isNull();
    assertThat(saved.getSex()).isNull();
  }

  @Test
  void withoutALanguage_keepsTheProfilesLanguage() throws ValidationException {
    Profile existing = Profile.createProfileFromId(USER_ID.toString());
    existing.setLanguage("en");
    when(profileRepository.findById(USER_ID)).thenReturn(Optional.of(existing));

    profileService.upsertDemographics(new UpsertDemographicsCommand(
        USER_ID.toString(), LocalDate.parse("1980-05-17"), Sex.MALE, null));

    assertThat(savedProfile().getLanguage()).isEqualTo("en");
  }

  @Test
  void newProfileWithoutALanguage_isSlovenian() throws ValidationException {
    when(profileRepository.findById(USER_ID)).thenReturn(Optional.empty());

    profileService.upsertDemographics(new UpsertDemographicsCommand(
        USER_ID.toString(), LocalDate.parse("1980-05-17"), Sex.MALE, null));

    assertThat(savedProfile().getLanguage()).isEqualTo("sl");
  }

  @Test
  void rejectsDateOfBirthNotInThePast() {
    var command = new UpsertDemographicsCommand(USER_ID.toString(), LocalDate.parse("2026-10-20"), Sex.MALE, "sl");

    var e = catchThrowableOfType(ValidationException.class,
        () -> profileService.upsertDemographics(command));

    assertThat(e).isNotNull();
    assertThat(e.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
    assertThat(e.getMessage()).doesNotContain("2026");
    verify(profileRepository, never()).save(any());
  }

  @Test
  void commandToStringDoesNotContainPersonalData() {
    var command = new UpsertDemographicsCommand(
        USER_ID.toString(), LocalDate.parse("1980-05-17"), Sex.FEMALE, "en");

    assertThat(command.toString())
        .doesNotContain("1980")
        .doesNotContain("05-17")
        .doesNotContain("FEMALE");
  }

  @Test
  void profileToStringDoesNotContainPersonalData() {
    Profile profile = Profile.createProfileFromId(USER_ID.toString());
    profile.setDateOfBirth(LocalDate.parse("1980-05-17"));
    profile.setSex(Sex.FEMALE);

    assertThat(profile.toString()).doesNotContain("1980").doesNotContain("FEMALE");
  }

  private Profile savedProfile() {
    ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
    verify(profileRepository).save(captor.capture());
    return captor.getValue();
  }
}
