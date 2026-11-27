package si.result.eearly.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import si.result.eearly.admin.AdminServiceClient;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.domain.profile.UpsertProfileCommand;
import si.result.eearly.ehr.EhrbaseClient;
import si.result.eearly.mapper.ProfileMapper;
import si.result.eearly.repository.ProfileRepository;
import si.result.eearly.service.push.PushNotificationService;

/** The patient's language: changed in the app (UpdateLanguage), sent on to the admin service. */
class ProfileServiceLanguageTest {

  private static final UUID USER_ID = UUID.randomUUID();

  private ProfileRepository profileRepository;
  private EhrbaseClient ehrbaseClient;
  private AdminServiceClient adminServiceClient;
  private ProfileServiceImpl profileService;

  @BeforeEach
  void setUp() {
    profileRepository = mock(ProfileRepository.class);
    ehrbaseClient = mock(EhrbaseClient.class);
    adminServiceClient = mock(AdminServiceClient.class);
    when(profileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    profileService = new ProfileServiceImpl(profileRepository, ehrbaseClient, Clock.systemUTC(),
        adminServiceClient, mock(PushNotificationService.class));
  }

  @Test
  void updateLanguage_savesItAndSendsItToTheAdminService() throws ValidationException {
    when(profileRepository.findById(USER_ID)).thenReturn(Optional.of(Profile.createProfileFromId(USER_ID.toString())));

    Profile result = profileService.updateLanguage(USER_ID.toString(), "en");

    assertThat(result.getLanguage()).isEqualTo("en");
    assertThat(savedProfile().getLanguage()).isEqualTo("en");
    verify(adminServiceClient).changePatientLanguage(USER_ID, "en");
  }

  @Test
  void updateLanguage_stillSucceedsWhenTheAdminServiceCannotBeReached() throws ValidationException {
    when(profileRepository.findById(USER_ID)).thenReturn(Optional.of(Profile.createProfileFromId(USER_ID.toString())));
    doThrow(new IllegalStateException("connection refused"))
        .when(adminServiceClient).changePatientLanguage(USER_ID, "en");

    Profile result = profileService.updateLanguage(USER_ID.toString(), "en");

    assertThat(result.getLanguage()).isEqualTo("en");
    assertThat(savedProfile().getLanguage()).isEqualTo("en");
  }

  @Test
  void updateLanguage_rejectsAnUnsupportedLanguage() {
    for (String language : new String[]{null, "", "de"}) {
      var e = catchThrowableOfType(ValidationException.class,
          () -> profileService.updateLanguage(USER_ID.toString(), language));

      assertThat(e).isNotNull();
      assertThat(e.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
    verify(profileRepository, never()).save(any());
    verifyNoInteractions(adminServiceClient);
  }

  @Test
  void updateLanguage_failsForAnUnknownProfileWithoutCreatingOne() {
    when(profileRepository.findById(USER_ID)).thenReturn(Optional.empty());

    var e = catchThrowableOfType(ValidationException.class,
        () -> profileService.updateLanguage(USER_ID.toString(), "en"));

    assertThat(e).isNotNull();
    verify(profileRepository, never()).save(any());
    verifyNoInteractions(adminServiceClient);
  }

  @Test
  void appUpsertKeepsTheLanguage() throws ValidationException {
    Profile existing = Profile.createProfileFromId(USER_ID.toString());
    existing.setLanguage("en");
    when(profileRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
    when(ehrbaseClient.doesEhrExist(USER_ID)).thenReturn(true);

    profileService.upsertProfile(new UpsertProfileCommand(
        USER_ID.toString(), new byte[0], true, "20:00:00", false, 0, true, true));

    assertThat(savedProfile().getLanguage()).isEqualTo("en");
  }

  @Test
  void getProfileHandsTheLanguageToTheApp() {
    Profile profile = Profile.createProfileFromId(USER_ID.toString());
    profile.setLanguage("en");
    profile.setPicture(new byte[0]);
    profile.setNotificationDaySummaryTime(LocalTime.of(20, 0));

    var grpcProfile = Mappers.getMapper(ProfileMapper.class).toGrpcProfile(profile, "patient");

    assertThat(grpcProfile.getLanguage()).isEqualTo("en");
  }

  private Profile savedProfile() {
    ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
    verify(profileRepository).save(captor.capture());
    return captor.getValue();
  }
}
