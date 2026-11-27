package si.result.eearly.facade;

import java.util.UUID;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.exception.NotFoundException;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.UpsertProfileCommand;
import si.result.eearly.domain.user.UserToken;
import si.result.eearly.dto.CreateUserDTO;
import si.result.eearly.dto.CreateUserResponseDTO;
import si.result.eearly.dto.MobileConfigurationDTO;
import si.result.eearly.dto.OnboardingLinkDTO;
import si.result.eearly.mapper.OnboardingMapper;
import si.result.eearly.repository.ProfileRepository;
import si.result.eearly.service.KeycloakService;
import si.result.eearly.service.ProfileService;
import si.result.eearly.service.onboarding.AppStoreRedirectService;
import si.result.eearly.service.onboarding.DeviceDetectionService;
import si.result.eearly.service.onboarding.DeviceInfo;
import si.result.eearly.service.onboarding.MobileConfig;
import si.result.eearly.service.onboarding.OnboardingCodeService;

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
  private final OnboardingCodeService onboardingCodeService;
  private final ProfileRepository profileRepository;

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
    String code = onboardingCodeService.issue(UUID.fromString(user.id()));
    return onboardingMapper.toDTO(user, onboardingUrl(code));
  }

  /**
   * Issues a new onboarding link for an existing patient; the unused codes of earlier links stop
   * working.
   */
  @Transactional
  public OnboardingLinkDTO issueOnboardingLink(UUID userId) {
    if (!keycloakService.userExists(userId) || !profileRepository.existsById(userId)) {
      throw new NotFoundException("User not found");
    }
    return new OnboardingLinkDTO(onboardingUrl(onboardingCodeService.replace(userId)));
  }

  /**
   * Exchanges an onboarding code for the mobile configuration with the user's first tokens. The
   * code is marked used first; if issuing the tokens fails the transaction rolls back and the
   * code stays usable. Empty when the code is unknown, used, expired or malformed (which includes
   * a raw user id from the old link format).
   */
  @Transactional(rollbackFor = Exception.class)
  public Optional<MobileConfigurationDTO> getConfiguration(String code) throws ValidationException {
    Optional<UUID> userId = onboardingCodeService.redeem(code);
    if (userId.isEmpty()) {
      log.info("Onboarding code redemption: rejected");
      return Optional.empty();
    }
    UserToken token = keycloakService.getUserToken(userId.get());
    log.info("Onboarding code redemption: success, user {}", userId.get());
    return Optional.of(onboardingMapper.toDTO(mobileConfig, userId.get(), token));
  }

  private String onboardingUrl(String code) {
    return onBoardingBaseUrl + "/" + code;
  }

  public String getMobileStoreURL(String userAgent) {
    DeviceInfo deviceInfo = deviceDetectionService.detectDevice(userAgent);
    log.debug("Detected device: {}", deviceInfo);
    return appStoreRedirectService.getAppStoreUrl(deviceInfo.os());
  }
}
