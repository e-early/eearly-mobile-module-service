package si.result.eearly.ehr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class EhrKeycloakRestTemplateConfig {

  @Value("${ehr.keycloak.base-url}")
  private String baseUrl;

  @Bean(name = "ehrKeycloakRestTemplate")
  public RestTemplate ehrKeycloakRestTemplate(RestTemplateBuilder builder) {
    return builder
        .rootUri(baseUrl)
        .build();
  }
}