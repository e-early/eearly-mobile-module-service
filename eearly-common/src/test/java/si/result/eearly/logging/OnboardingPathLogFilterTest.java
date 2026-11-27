package si.result.eearly.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.Test;

class OnboardingPathLogFilterTest {

  private static final String USER = "7f9c2d3e-1111-4222-8333-444455556666";
  private static final String CODE = "q3hJ9s0Xf2yQp8LkVn1AaBcDeFgHiJkLmNoPqRsTuVw";

  private final OnboardingPathLogFilter filter = new OnboardingPathLogFilter();

  private FilterReply decide(String format, Object... params) {
    return filter.decide(null, null, null, format, params, null);
  }

  @Test
  void dropsRequestLinesWithAUserIdOrCodeInThePath() {
    assertThat(decide("Securing GET /api/v1/onboarding/" + USER + "/" + CODE + "/configuration"))
        .isEqualTo(FilterReply.DENY);
    assertThat(decide("Securing GET /api/v1/onboarding/" + USER + "/" + CODE))
        .isEqualTo(FilterReply.DENY);
    assertThat(decide("Securing GET /api/v1/onboarding/" + USER)).isEqualTo(FilterReply.DENY);
    assertThat(decide("Securing GET /api/v1/onboarding/" + USER + "/" + CODE + "?token=qr"))
        .isEqualTo(FilterReply.DENY);
    assertThat(decide("GET \"/api/v1/onboarding/" + USER + "/" + CODE + "\", parameters={}"))
        .isEqualTo(FilterReply.DENY);
  }

  @Test
  void dropsParametersAndThrowableMessagesWithThePath() {
    assertThat(decide("Request {}", "/api/v1/onboarding/" + USER + "/" + CODE))
        .isEqualTo(FilterReply.DENY);
    assertThat(filter.decide(null, null, null, "failed", null,
        new IllegalStateException("No endpoint GET /api/v1/onboarding/" + USER + "/" + CODE)))
        .isEqualTo(FilterReply.DENY);
  }

  @Test
  void dropsTheCodeEvenAfterTheUsersSegment() {
    // "users" as a user id must not open a way around the masking
    assertThat(decide("Securing GET /api/v1/onboarding/users/" + CODE))
        .isEqualTo(FilterReply.DENY);
  }

  @Test
  void keepsEndpointsThatCarryNoCode() {
    assertThat(decide("Securing POST /api/v1/onboarding/create-user")).isEqualTo(FilterReply.NEUTRAL);
    assertThat(decide("Securing POST /api/v1/onboarding/users/" + USER + "/onboarding-link"))
        .isEqualTo(FilterReply.NEUTRAL);
    assertThat(decide("Securing GET /api/v1/profile")).isEqualTo(FilterReply.NEUTRAL);
    assertThat(decide("Securing GET /api/v1/onboarding")).isEqualTo(FilterReply.NEUTRAL);
    assertThat(decide("plain message")).isEqualTo(FilterReply.NEUTRAL);
    assertThat(decide(null)).isEqualTo(FilterReply.NEUTRAL);
  }
}
