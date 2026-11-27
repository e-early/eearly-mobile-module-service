package si.result.eearly.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

/**
 * Outside the {@code development} profile the service logs one ECS JSON object per line to the
 * console; ELK ingestion and dashboards depend on these fields. Any change to the logging
 * configuration must keep this test passing.
 */
@SpringBootTest(classes = LogFormatCharacterizationTest.Config.class,
    properties = "spring.application.name=eearly-log-format-test")
@ExtendWith(OutputCaptureExtension.class)
class LogFormatCharacterizationTest {

  @SpringBootConfiguration
  static class Config {}

  private static final String MARKER = "log-format-characterization-marker";

  @Test
  void consoleLinesAreEcsJson(CapturedOutput output) throws Exception {
    LoggerFactory.getLogger(LogFormatCharacterizationTest.class).info(MARKER);

    String line = Arrays.stream(output.getOut().split("\\R"))
        .filter(l -> l.contains(MARKER)).findFirst().orElseThrow();
    JsonNode json = new ObjectMapper().readTree(line);

    List<String> fields = new ArrayList<>();
    json.fieldNames().forEachRemaining(fields::add);
    assertThat(fields).as("ECS fields in: %s", fields)
        .contains("@timestamp", "log.level", "message", "log.logger", "process.thread.name",
            "service.name", "ecs.version");
    assertThat(json.get("message").asText()).isEqualTo(MARKER);
    assertThat(json.get("log.level").asText()).isEqualTo("INFO");
    assertThat(json.get("service.name").asText()).isEqualTo("eearly-log-format-test");
  }

  /** DEBUG output contains token claims and request bodies; it must be off unless asked for. */
  @Test
  void debugLoggingIsOffByDefault() {
    assertThat(LoggerFactory.getLogger("si.result.eearly.service.UserPrincipalService")
        .isDebugEnabled()).isFalse();
    assertThat(LoggerFactory.getLogger("org.springframework.web.client.RestTemplate")
        .isDebugEnabled()).isFalse();
  }

  /** Request lines under /api/v1/onboarding/ carry a one-time code; logback-spring.xml drops them. */
  @Test
  void onboardingCodesNeverReachTheLog(CapturedOutput output) {
    String code = "q3hJ9s0Xf2yQp8LkVn1AaBcDeFgHiJkLmNoPqRsTuVw";
    var logger = LoggerFactory.getLogger(LogFormatCharacterizationTest.class);

    logger.info("Securing GET /api/v1/onboarding/7f9c2d3e-1111-4222-8333-444455556666/{}/configuration",
        code);
    logger.info("Request {}", "/api/v1/onboarding/7f9c2d3e-1111-4222-8333-444455556666/" + code);
    logger.info("onboarding-path-control-marker POST /api/v1/onboarding/create-user");

    assertThat(output.getOut()).doesNotContain(code)
        .contains("onboarding-path-control-marker");
  }
}
