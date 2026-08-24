package si.result.eearly.keycloak;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

  @Value("${keycloak.auth-server-url}")
  private String keycloakServerUrl;

  @Value("${keycloak.realm}")
  private String keycloakRealm;

  @Value("${keycloak.admin.client.id}")
  private String keycloakClientId;

  @Value("${keycloak.admin.client.secret}")
  private String keycloakClientSecret;

  @Bean
  public Keycloak keycloak() {
    return KeycloakBuilder.builder()
            .serverUrl(keycloakServerUrl)
            .realm(keycloakRealm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientSecret(keycloakClientSecret)
            .clientId(keycloakClientId)
            .build();
  }
}
