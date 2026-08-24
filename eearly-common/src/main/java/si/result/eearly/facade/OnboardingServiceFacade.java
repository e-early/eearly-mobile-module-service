package si.result.eearly.facade;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.UpsertProfileCommand;
import si.result.eearly.domain.user.UserToken;
import si.result.eearly.dto.CreateUserDTO;
import si.result.eearly.dto.CreateUserResponseDTO;
import si.result.eearly.dto.MobileConfigurationDTO;
import si.result.eearly.mapper.OnboardingMapper;
import si.result.eearly.service.KeycloakService;
import si.result.eearly.service.ProfileService;
import si.result.eearly.service.onboarding.AppStoreRedirectService;
import si.result.eearly.service.onboarding.DeviceDetectionService;
import si.result.eearly.service.onboarding.DeviceInfo;
import si.result.eearly.service.onboarding.MobileConfig;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingServiceFacade {
  private final KeycloakService keycloakService;
  private final ProfileService profileService;
  private final OnboardingMapper onboardingMapper;
  private final DeviceDetectionService deviceDetectionService;
  private final AppStoreRedirectService appStoreRedirectService;
  private final MobileConfig mobileConfig;

  @Value("${onboarding.app.base-url}")
  private String onBoardingBaseUrl;

  @Transactional
  public CreateUserResponseDTO createUser(CreateUserDTO createUserDTO) throws ValidationException {
    var user = onboardingMapper.toDTO(keycloakService.createUser(onboardingMapper.toCreateCommand(createUserDTO)));

    final var upsertProfileCommand = new UpsertProfileCommand(
        user.id(),
        null,
        true,
        "08:00:00",
        true,
        15,
        true,
        true
    );

    profileService.upsertProfile(upsertProfileCommand);
    String onboardingUrl = onBoardingBaseUrl + "/" + user.id();
    return onboardingMapper.toDTO(user, onboardingUrl);
  }

  public MobileConfigurationDTO getConfiguration(String onboardingId) throws ValidationException {
    UUID userId = UUID.fromString(onboardingId);
    UserToken token = keycloakService.getUserToken(userId);
    return onboardingMapper.toDTO(mobileConfig, userId, token);
  }

  public String getMobileStoreURL(String userAgent) {
    log.info("Trying to determine device from user agent: {}", userAgent);
    DeviceInfo deviceInfo = deviceDetectionService.detectDevice(userAgent);
    log.info("Detected device: {}", deviceInfo);
    return appStoreRedirectService.getAppStoreUrl(deviceInfo.os());
  }
}
