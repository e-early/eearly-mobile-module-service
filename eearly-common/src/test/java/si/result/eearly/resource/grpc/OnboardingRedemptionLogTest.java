package si.result.eearly.resource.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import si.result.eearly.WebSecurityConfiguration;
import si.result.eearly.domain.user.UserToken;
import si.result.eearly.facade.OnboardingServiceFacade;
import si.result.eearly.mapper.OnboardingMapper;
import si.result.eearly.repository.ProfileRepository;
import si.result.eearly.service.KeycloakService;
import si.result.eearly.service.ProfileService;
import si.result.eearly.service.onboarding.AppStoreRedirectService;
import si.result.eearly.service.onboarding.DeviceDetectionService;
import si.result.eearly.service.onboarding.DeviceInfo;
import si.result.eearly.service.onboarding.MobileConfig;
import si.result.eearly.service.onboarding.OnboardingCodeService;
import si.result.eearly.service.onboarding.OnboardingRateLimiter;

/**
 * A redemption request, with the real security filters and the real facade, must not write the
 * code, its hash, the QR token, the tokens or the link to the log, at the test profile's verbose
 * (DEBUG) log levels.
 */
@WebMvcTest
@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(properties = {
    "keycloak.admin-service.client.id=admin-service-test",
    "spring.security.cors.allowed-origins=*",
    "spring.security.cors.allowed-methods=GET,POST"
})
class OnboardingRedemptionLogTest {

  private static final UUID USER = UUID.fromString("7f9c2d3e-1111-4222-8333-444455556666");
  private static final String CODE = OnboardingCodeService.generateCode();
  private static final String QR_TOKEN = "qr-token-4f1c9a";

  @SpringBootConfiguration
  @Import({WebSecurityConfiguration.class, OnboardingController.class,
      OnboardingRateLimiter.class})
  static class Config {
    @Bean
    Clock clock() {
      return Clock.fixed(Instant.parse("2026-03-01T10:00:10Z"), ZoneOffset.UTC);
    }

    @Bean
    OnboardingServiceFacade facade(KeycloakService keycloakService,
        OnboardingCodeService codeService, DeviceDetectionService deviceDetectionService,
        AppStoreRedirectService appStoreRedirectService) {
      OnboardingServiceFacade facade = new OnboardingServiceFacade(keycloakService,
          mock(ProfileService.class), Mappers.getMapper(OnboardingMapper.class),
          deviceDetectionService, appStoreRedirectService,
          mock(MobileConfig.class), codeService, mock(ProfileRepository.class));
      ReflectionTestUtils.setField(facade, "onBoardingBaseUrl", "https://onboarding.example.test");
      return facade;
    }
  }

  @Autowired private MockMvc mvc;
  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private KeycloakService keycloakService;
  @MockitoBean private OnboardingCodeService codeService;
  @MockitoBean private DeviceDetectionService deviceDetectionService;
  @MockitoBean private AppStoreRedirectService appStoreRedirectService;

  @Test
  void aSuccessfulRedemptionLogsNoCodeHashTokenOrLink(CapturedOutput output) throws Exception {
    when(codeService.redeem(CODE)).thenReturn(Optional.of(USER));
    when(keycloakService.getUserToken(USER))
        .thenReturn(new UserToken("access-token-value-77", "refresh-token-value-88"));

    mvc.perform(get("/api/v1/onboarding/" + CODE + "/configuration")
            .param("token", QR_TOKEN))
        .andExpect(status().isOk());

    String log = output.getAll();
    assertThat(log).contains("Onboarding code redemption: success").contains(USER.toString());
    assertNothingSensitive(log);
  }

  @Test
  void aRejectedRedemptionLogsOnlyTheOutcome(CapturedOutput output) throws Exception {
    when(codeService.redeem(CODE)).thenReturn(Optional.empty());

    mvc.perform(get("/api/v1/onboarding/" + CODE + "/configuration").param("token", QR_TOKEN))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/onboarding/" + CODE + "/configuration"))
        .andExpect(status().isNotFound());

    String log = output.getAll();
    assertThat(log).contains("Onboarding code redemption: rejected");
    assertNothingSensitive(log);
  }

  @Test
  void openingTheLinkRedirectsWithoutUsingUpTheCodeAndLogsNothingSensitive(CapturedOutput output)
      throws Exception {
    when(deviceDetectionService.detectDevice(any())).thenReturn(new DeviceInfo("ios", "phone"));
    when(appStoreRedirectService.getAppStoreUrl("ios")).thenReturn("https://store.example.test/app");
    when(codeService.redeem(CODE)).thenReturn(Optional.of(USER));
    when(keycloakService.getUserToken(USER))
        .thenReturn(new UserToken("access-token-value-77", "refresh-token-value-88"));

    mvc.perform(get("/api/v1/onboarding/" + CODE).param("token", QR_TOKEN)
            .header("User-Agent", "Mozilla/5.0 (iPhone) unique-agent-marker"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://store.example.test/app"));

    verify(codeService, never()).redeem(any());
    // the code is still there for the app
    mvc.perform(get("/api/v1/onboarding/" + CODE + "/configuration"))
        .andExpect(status().isOk());
    verify(codeService).redeem(CODE);

    assertNothingSensitive(output.getAll());
    assertThat(output.getAll()).doesNotContain("unique-agent-marker");
  }

  private static void assertNothingSensitive(String log) {
    assertThat(log)
        .doesNotContain(CODE)
        .doesNotContain(OnboardingCodeService.hash(CODE))
        .doesNotContain(QR_TOKEN)
        .doesNotContain("access-token-value-77")
        .doesNotContain("refresh-token-value-88")
        .doesNotContain("https://onboarding.example.test");
  }
}
