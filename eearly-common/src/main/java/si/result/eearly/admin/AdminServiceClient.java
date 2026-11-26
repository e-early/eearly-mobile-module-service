package si.result.eearly.admin;

import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Calls the admin service's internal REST API ({@code /api/v1/internal/**}) as this service's own
 * Keycloak client (client credentials, admin realm), whose service account has the admin UI
 * client's {@code MobileService} role.
 *
 * <p>The admin service owns patient data and normally pushes it here (BackofficePatientService);
 * this is the one way back, for what the patient changes in the app.
 */
@Slf4j
@Service
public class AdminServiceClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;
  private final String keycloakBaseUrl;
  private final String realm;
  private final String clientId;
  private final String clientSecret;

  public AdminServiceClient(
      @Qualifier("restTemplate") RestTemplate restTemplate,
      @Value("${admin-service.base-url:}") String baseUrl,
      @Value("${admin-service.keycloak.base-url:}") String keycloakBaseUrl,
      @Value("${admin-service.keycloak.realm:eearly-admin}") String realm,
      @Value("${admin-service.keycloak.client-id:eearly-mobile-service}") String clientId,
      @Value("${admin-service.keycloak.client-secret:}") String clientSecret) {
    this.restTemplate = restTemplate;
    this.baseUrl = stripTrailingSlash(baseUrl);
    this.keycloakBaseUrl = stripTrailingSlash(keycloakBaseUrl);
    this.realm = realm;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  /** False until the admin service's URL and this service's admin realm client are configured. */
  public boolean isConfigured() {
    return !baseUrl.isBlank() && !keycloakBaseUrl.isBlank() && !clientSecret.isBlank();
  }

  /**
   * Tells the admin service the language the patient picked in the app. Throws on any failure;
   * callers decide whether that matters. Does nothing (and logs) when not configured.
   */
  public void changePatientLanguage(UUID keycloakId, String language) {
    if (!isConfigured()) {
      log.info("Admin service client not configured, not sending the language of patient {}", keycloakId);
      return;
    }
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(fetchAccessToken());
    restTemplate.exchange(
        baseUrl + "/api/v1/internal/patients/{keycloakId}/language",
        HttpMethod.PUT,
        new HttpEntity<>(Map.of("language", language), headers),
        Void.class,
        keycloakId);
  }

  // Not cached: a patient changes their language rarely, so one token request per change is fine.
  String fetchAccessToken() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "client_credentials");
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        keycloakBaseUrl + "/realms/{realm}/protocol/openid-connect/token",
        HttpMethod.POST,
        new HttpEntity<>(body, headers),
        new ParameterizedTypeReference<>() {
        },
        realm);
    Map<String, Object> responseBody = response.getBody();
    if (responseBody == null || !(responseBody.get("access_token") instanceof String token)) {
      throw new IllegalStateException("Admin realm token response has no access_token");
    }
    return token;
  }

  private static String stripTrailingSlash(String url) {
    if (url == null) {
      return "";
    }
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }
}
