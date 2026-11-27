package si.result.eearly.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;
import si.result.eearly.domain.exception.NotFoundException;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.domain.user.User;
import si.result.eearly.domain.user.UserToken;
import si.result.eearly.dto.CreateUserDTO;
import si.result.eearly.dto.MobileConfigurationDTO;
import si.result.eearly.mapper.OnboardingMapper;
import si.result.eearly.repository.ProfileRepository;
import si.result.eearly.service.KeycloakService;
import si.result.eearly.service.ProfileService;
import si.result.eearly.service.onboarding.AppStoreRedirectService;
import si.result.eearly.service.onboarding.DeviceDetectionService;
import si.result.eearly.service.onboarding.MobileConfig;
import si.result.eearly.service.onboarding.OnboardingCodeService;

class OnboardingServiceFacadeTest {

  private static final String BASE_URL = "https://onboarding.example.test";
  private static final String CODE = OnboardingCodeService.generateCode();

  private final KeycloakService keycloakService = mock(KeycloakService.class);
  private final ProfileService profileService = mock(ProfileService.class);
  private final OnboardingCodeService codeService = mock(OnboardingCodeService.class);
  private final ProfileRepository profileRepository = mock(ProfileRepository.class);
  private final UUID userId = UUID.randomUUID();
  private OnboardingServiceFacade facade;

  @BeforeEach
  void setUp() {
    facade = new OnboardingServiceFacade(keycloakService, profileService,
        Mappers.getMapper(OnboardingMapper.class), mock(DeviceDetectionService.class),
        mock(AppStoreRedirectService.class), mock(MobileConfig.class), codeService,
        profileRepository);
    ReflectionTestUtils.setField(facade, "onBoardingBaseUrl", BASE_URL);
  }

  @Test
  void createUserReturnsALinkWithTheUserIdAndTheNewCode() throws Exception {
    when(keycloakService.createUser(any()))
        .thenReturn(new User(userId.toString(), "Ana", "Test", "ana@example.test", "testana00001"));
    when(codeService.issue(userId)).thenReturn(CODE);

    var response = facade.createUser(new CreateUserDTO("Ana", "Test", "ana@example.test"));

    assertThat(response.onboardingUrl()).isEqualTo(BASE_URL + "/" + CODE);
    assertThat(response.user().id()).isEqualTo(userId.toString());
  }

  @Test
  void validCodeGivesTheTokensForTheStoredUser() throws Exception {
    when(codeService.redeem(CODE)).thenReturn(Optional.of(userId));
    when(keycloakService.getUserToken(userId)).thenReturn(new UserToken("access", "refresh"));

    Optional<MobileConfigurationDTO> config = facade.getConfiguration(CODE);

    assertThat(config).isPresent();
    assertThat(config.get().userId()).isEqualTo(userId);
    assertThat(config.get().accessToken()).isEqualTo("access");
    assertThat(config.get().refreshToken()).isEqualTo("refresh");
  }

  @Test
  void aCodeIsRedeemedOnlyOnce() throws Exception {
    when(codeService.redeem(CODE)).thenReturn(Optional.of(userId), Optional.empty());
    when(keycloakService.getUserToken(userId)).thenReturn(new UserToken("access", "refresh"));

    assertThat(facade.getConfiguration(CODE)).isPresent();
    assertThat(facade.getConfiguration(CODE)).isEmpty();

    verify(keycloakService).getUserToken(userId);
  }

  @Test
  void rejectedCodeAndRawUserIdIssueNoTokens() throws Exception {
    when(codeService.redeem(any())).thenReturn(Optional.empty());

    assertThat(facade.getConfiguration(CODE)).isEmpty();
    assertThat(facade.getConfiguration(userId.toString())).isEmpty();

    verify(keycloakService, never()).getUserToken(any());
  }

  @Test
  void issueOnboardingLinkReplacesTheEarlierCodes() {
    when(keycloakService.userExists(userId)).thenReturn(true);
    when(profileRepository.existsById(userId)).thenReturn(true);
    when(codeService.replace(userId)).thenReturn(CODE);

    assertThat(facade.issueOnboardingLink(userId).onboardingUrl())
        .isEqualTo(BASE_URL + "/" + CODE);
    verify(codeService).replace(userId);
  }

  @Test
  void issueOnboardingLinkIsNotFoundForAnUnknownKeycloakUser() {
    when(keycloakService.userExists(userId)).thenReturn(false);
    when(profileRepository.existsById(userId)).thenReturn(true);

    assertThatThrownBy(() -> facade.issueOnboardingLink(userId))
        .isInstanceOf(NotFoundException.class);
    verifyNoInteractions(codeService);
  }

  @Test
  void issueOnboardingLinkIsNotFoundWithoutAProfile() {
    when(keycloakService.userExists(userId)).thenReturn(true);
    when(profileRepository.existsById(userId)).thenReturn(false);

    assertThatThrownBy(() -> facade.issueOnboardingLink(userId))
        .isInstanceOf(NotFoundException.class);
    verifyNoInteractions(codeService);
  }
}
