package si.result.eearly.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class AdminServiceClientTest {

  private static final UUID KEYCLOAK_ID = UUID.fromString("50287036-1b84-4e2e-831f-91ab621e52d9");

  @Test
  void changePatientLanguage_fetchesAnAdminRealmTokenAndPutsTheLanguage() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    AdminServiceClient client = new AdminServiceClient(restTemplate,
        "http://admin.example/", "http://keycloak.example", "eearly-admin", "eearly-mobile-service", "secret");

    server.expect(requestTo("http://keycloak.example/realms/eearly-admin/protocol/openid-connect/token"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("grant_type=client_credentials")))
        .andRespond(withSuccess("{\"access_token\":\"token-123\"}", MediaType.APPLICATION_JSON));
    server.expect(requestTo("http://admin.example/api/v1/internal/patients/" + KEYCLOAK_ID + "/language"))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header("Authorization", "Bearer token-123"))
        .andExpect(content().json("{\"language\":\"en\"}"))
        .andRespond(withNoContent());

    client.changePatientLanguage(KEYCLOAK_ID, "en");

    server.verify();
  }

  @Test
  void changePatientLanguage_whenNotConfigured_sendsNothing() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    AdminServiceClient client = new AdminServiceClient(restTemplate, "", "", "eearly-admin", "eearly-mobile-service", "");

    assertThat(client.isConfigured()).isFalse();
    client.changePatientLanguage(KEYCLOAK_ID, "en");

    server.verify();
  }
}
