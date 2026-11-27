package si.result.eearly.resource.grpc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import si.result.eearly.WebSecurityConfiguration;
import si.result.eearly.domain.exception.NotFoundException;
import si.result.eearly.dto.CreateUserResponseDTO;
import si.result.eearly.dto.MobileConfigurationDTO;
import si.result.eearly.dto.OnboardingLinkDTO;
import si.result.eearly.dto.UserDTO;
import si.result.eearly.facade.OnboardingServiceFacade;
import si.result.eearly.service.onboarding.OnboardingCodeService;
import si.result.eearly.service.onboarding.OnboardingRateLimiter;

/** The onboarding endpoints behind the real security configuration; Keycloak and the database are mocked. */
@WebMvcTest
@TestPropertySource(properties = {
    "keycloak.admin-service.client.id=admin-service-test",
    "spring.security.cors.allowed-origins=*",
    "spring.security.cors.allowed-methods=GET,POST"
})
class OnboardingEndpointSecurityTest {

  @SpringBootConfiguration
  @Import({WebSecurityConfiguration.class, OnboardingController.class,
      OnboardingRateLimiter.class})
  static class Config {
    @Bean
    Clock clock() {
      return Clock.fixed(Instant.parse("2026-03-01T10:00:10Z"), ZoneOffset.UTC);
    }
  }

  private static final UUID USER = UUID.fromString("7f9c2d3e-1111-4222-8333-444455556666");
  private static final String CODE = OnboardingCodeService.generateCode();
  private static final String NOT_FOUND_BODY = "{\"status\":404,\"error\":\"Not found\"}";
  private static final AtomicInteger NEXT_CLIENT = new AtomicInteger(1);

  @Autowired private MockMvc mvc;
  @MockitoBean private OnboardingServiceFacade facade;
  @MockitoBean private JwtDecoder jwtDecoder;

  @BeforeEach
  void tokens() {
    when(jwtDecoder.decode("admin")).thenReturn(jwt(List.of("onboard-patient")));
    when(jwtDecoder.decode("patient")).thenReturn(jwt(List.of("patient")));
  }

  private static Jwt jwt(List<String> roles) {
    return Jwt.withTokenValue("token").header("alg", "none").subject(USER.toString())
        .claim("realm_access", Map.of("roles", roles))
        .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
  }

  /** Every test gets its own client address so the rate limit does not carry over. */
  private static MockHttpServletRequestBuilder from(MockHttpServletRequestBuilder builder,
      String address) {
    return builder.with(request -> {
      request.setRemoteAddr(address);
      return request;
    });
  }

  private static String newClient() {
    return "192.0.2." + NEXT_CLIENT.getAndIncrement();
  }

  private MockHttpServletRequestBuilder createUserRequest() {
    return post("/api/v1/onboarding/create-user").contentType(MediaType.APPLICATION_JSON)
        .content("{\"firstName\":\"Ana\",\"lastName\":\"Test\",\"email\":\"ana@example.test\"}");
  }

  private static MobileConfigurationDTO configuration() {
    return new MobileConfigurationDTO("https://keycloak.example.test", "realm", "client", "host",
        USER, "access-token-value", "refresh-token-value", "https://api.example.test");
  }

  // --- create-user and onboarding-link need the onboard-patient role ---

  @Test
  void createUserRequiresTheOnboardPatientRole() throws Exception {
    when(facade.createUser(any())).thenReturn(new CreateUserResponseDTO(
        new UserDTO(USER.toString(), "Ana", "Test", "ana@example.test", "testana00001"),
        "https://onboarding.example.test/" + CODE));

    mvc.perform(createUserRequest()).andExpect(status().isUnauthorized());
    mvc.perform(createUserRequest().header("Authorization", "Bearer patient"))
        .andExpect(status().isForbidden());
    mvc.perform(createUserRequest().header("Authorization", "Bearer admin"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.onboardingUrl").value(
            "https://onboarding.example.test/" + CODE));
  }

  @Test
  void onboardingLinkRequiresTheOnboardPatientRole() throws Exception {
    String path = "/api/v1/onboarding/users/" + USER + "/onboarding-link";
    when(facade.issueOnboardingLink(USER)).thenReturn(
        new OnboardingLinkDTO("https://onboarding.example.test/" + CODE));

    mvc.perform(post(path)).andExpect(status().isUnauthorized());
    mvc.perform(post(path).header("Authorization", "Bearer patient"))
        .andExpect(status().isForbidden());
    mvc.perform(post(path).header("Authorization", "Bearer admin"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.onboardingUrl").value(
            "https://onboarding.example.test/" + CODE));
  }

  @Test
  void onboardingLinkForAnUnknownUserIsNotFound() throws Exception {
    when(facade.issueOnboardingLink(USER)).thenThrow(new NotFoundException("User not found"));

    mvc.perform(post("/api/v1/onboarding/users/" + USER + "/onboarding-link")
            .header("Authorization", "Bearer admin"))
        .andExpect(status().isNotFound());
    mvc.perform(post("/api/v1/onboarding/users/not-a-uuid/onboarding-link")
            .header("Authorization", "Bearer admin"))
        .andExpect(status().isNotFound());
  }

  @Test
  void theGetRoutesDoNotShadowTheOnboardingLinkEndpoint() throws Exception {
    String path = "/api/v1/onboarding/users/" + USER + "/onboarding-link";

    mvc.perform(get(path)).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/onboarding/create-user")).andExpect(status().isUnauthorized());
    mvc.perform(get(path).header("Authorization", "Bearer patient"))
        .andExpect(status().isForbidden());
  }

  // --- redemption is anonymous ---

  @Test
  void redemptionIsAllowedWithoutAuthentication() throws Exception {
    when(facade.getConfiguration(CODE)).thenReturn(Optional.of(configuration()));

    mvc.perform(from(get("/api/v1/onboarding/" + CODE + "/configuration"), newClient()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token-value"))
        .andExpect(jsonPath("$.userId").value(USER.toString()));
  }

  @Test
  void openingTheLinkOnlyRedirectsToTheAppStoreAndNeverRedeems() throws Exception {
    when(facade.getMobileStoreURL(any())).thenReturn("https://store.example.test/app");

    mvc.perform(from(get("/api/v1/onboarding/" + CODE).param("token", "qr-token"), newClient()))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://store.example.test/app"));
    mvc.perform(from(get("/api/v1/onboarding/" + USER), newClient()))
        .andExpect(status().isFound());

    verify(facade, never()).getConfiguration(any());
  }

  @Test
  void theQrTokenQueryParameterIsIgnored() throws Exception {
    when(facade.getConfiguration(CODE)).thenReturn(Optional.of(configuration()));

    mvc.perform(from(get("/api/v1/onboarding/" + CODE + "/configuration")
            .param("token", "qr-token"), newClient()))
        .andExpect(status().isOk());
    verify(facade).getConfiguration(CODE);
  }

  @Test
  void everyRejectedRedemptionGetsTheSameNotFound() throws Exception {
    // unknown, used, expired, malformed or a raw user id (old link): the facade answers empty
    // (only /configuration redeems; the bare link just redirects)
    when(facade.getConfiguration(any())).thenReturn(Optional.empty());
    String client = newClient();

    for (String path : List.of(
        "/api/v1/onboarding/" + CODE + "/configuration",
        "/api/v1/onboarding/" + USER + "/configuration",
        "/api/v1/onboarding/short/configuration")) {
      mvc.perform(from(get(path), client))
          .andExpect(status().isNotFound())
          .andExpect(content().json(NOT_FOUND_BODY, true));
    }
  }

  @Test
  void theAppStoreRedirectIsAnonymous() throws Exception {
    when(facade.getMobileStoreURL(any())).thenReturn("https://store.example.test/app");

    mvc.perform(from(get("/api/v1/onboarding"), newClient()))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://store.example.test/app"));
  }

  // --- everything else stays authenticated ---

  @Test
  void otherPathsStillRequireAuthentication() throws Exception {
    List<String> paths = List.of(
        "/api/v1/profile",
        "/api/v1/onboarding/" + CODE + "/configuration/extra",
        "/api/v1/onboarding/" + USER + "/" + CODE + "/configuration",
        "/eearly-admin-service/api/v1/onboarding/" + CODE + "/configuration",
        "/eearly-admin-service/api/v1/onboarding/" + USER);
    for (String path : paths) {
      mvc.perform(from(get(path), newClient())).andExpect(status().isUnauthorized());
    }
    // only GET is anonymous
    mvc.perform(post("/api/v1/onboarding/" + CODE)).andExpect(status().isUnauthorized());
    mvc.perform(post("/api/v1/onboarding/" + CODE + "/configuration"))
        .andExpect(status().isUnauthorized());
  }

  // --- rate limit ---

  @Test
  void theEleventhRequestInAMinuteIsRejectedAndOtherClientsAreNotAffected() throws Exception {
    when(facade.getConfiguration(any())).thenReturn(Optional.empty());
    String path = "/api/v1/onboarding/" + CODE + "/configuration";
    String client = newClient();

    for (int i = 1; i <= 10; i++) {
      mvc.perform(from(get(path), client)).andExpect(status().isNotFound());
    }
    mvc.perform(from(get(path), client))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"));
    mvc.perform(from(get(path), newClient())).andExpect(status().isNotFound());
  }

  @Test
  void allAnonymousGetRoutesShareTheLimit() throws Exception {
    when(facade.getMobileStoreURL(any())).thenReturn("https://store.example.test/app");
    String client = newClient();
    List<String> paths = List.of(
        "/api/v1/onboarding",
        "/api/v1/onboarding/" + CODE,
        "/api/v1/onboarding/" + CODE + "/configuration");
    for (int i = 0; i < 10; i++) {
      mvc.perform(from(get(paths.get(i % paths.size())), client));
    }

    for (String path : paths) {
      mvc.perform(from(get(path), client)).andExpect(status().isTooManyRequests());
    }
  }
}
