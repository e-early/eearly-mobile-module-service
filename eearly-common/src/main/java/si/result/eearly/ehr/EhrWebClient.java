package si.result.eearly.ehr;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class EhrWebClient {
  @Value("${ehr.base-uri}")
  private String baseUri;

  @Bean(name = "ehrbaseWebClient")
  public WebClient ehrbaseWebClient() {
    return WebClient.builder()
        .baseUrl(baseUri)
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs()
                .maxInMemorySize(2 * 1024 * 1024))
            .build())
        .build();
  }
}
