package si.result.eearly.service.onboarding;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration()
@Getter
public class MobileConfig {

    @Value("${onboarding.keycloak.base-url}")
    private String baseUrl;

    @Value("${onboarding.keycloak.realm}")
    private String realm;

    @Value("${onboarding.keycloak.client-id}")
    private String clientId;

    @Value("${onboarding.keycloak.host}")
    private String host;

    @Value("${onboarding.api_base_url}")
    private String apiBaseUrl;
}
