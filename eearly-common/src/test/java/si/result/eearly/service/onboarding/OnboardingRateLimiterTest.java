package si.result.eearly.service.onboarding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class OnboardingRateLimiterTest {

  /** A clock the test can move. */
  private static final class MovableClock extends Clock {
    private Instant now = Instant.parse("2026-03-01T10:00:10Z");

    void advance(Duration duration) {
      now = now.plus(duration);
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return now;
    }
  }

  private final MovableClock clock = new MovableClock();

  private static MockHttpServletRequest request(String remoteAddr, String forwardedFor) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr(remoteAddr);
    if (forwardedFor != null) {
      request.addHeader("X-Forwarded-For", forwardedFor);
    }
    return request;
  }

  @Test
  void allowsTenRequestsPerMinuteAndRejectsTheEleventh() {
    OnboardingRateLimiter limiter = new OnboardingRateLimiter(clock, 10, false);
    MockHttpServletRequest request = request("10.0.0.1", null);

    for (int i = 1; i <= 10; i++) {
      assertThat(limiter.tryAcquire(request)).as("request %d", i).isTrue();
    }
    assertThat(limiter.tryAcquire(request)).as("request 11").isFalse();
    assertThat(limiter.tryAcquire(request)).as("request 12").isFalse();
  }

  @Test
  void clientsAreCountedSeparately() {
    OnboardingRateLimiter limiter = new OnboardingRateLimiter(clock, 10, false);
    MockHttpServletRequest first = request("10.0.0.1", null);

    for (int i = 0; i < 11; i++) {
      limiter.tryAcquire(first);
    }

    assertThat(limiter.tryAcquire(first)).isFalse();
    assertThat(limiter.tryAcquire(request("10.0.0.2", null))).isTrue();
  }

  @Test
  void theLimitStartsOverInTheNextWindow() {
    OnboardingRateLimiter limiter = new OnboardingRateLimiter(clock, 2, false);
    MockHttpServletRequest request = request("10.0.0.1", null);
    limiter.tryAcquire(request);
    limiter.tryAcquire(request);
    assertThat(limiter.tryAcquire(request)).isFalse();

    clock.advance(Duration.ofSeconds(60));

    assertThat(limiter.tryAcquire(request)).isTrue();
  }

  @Test
  void forwardedForIsIgnoredByDefault() {
    OnboardingRateLimiter limiter = new OnboardingRateLimiter(clock, 1, false);

    assertThat(limiter.tryAcquire(request("10.0.0.1", "203.0.113.1"))).isTrue();
    // the client cannot get a fresh allowance by changing the header
    assertThat(limiter.tryAcquire(request("10.0.0.1", "203.0.113.2"))).isFalse();
  }

  @Test
  void forwardedForFirstHopIsUsedWhenTrusted() {
    OnboardingRateLimiter limiter = new OnboardingRateLimiter(clock, 1, true);

    assertThat(limiter.tryAcquire(request("10.0.0.1", "203.0.113.1, 198.51.100.7"))).isTrue();
    assertThat(limiter.tryAcquire(request("10.0.0.1", "203.0.113.1, 198.51.100.8"))).isFalse();
    assertThat(limiter.tryAcquire(request("10.0.0.1", "203.0.113.2"))).isTrue();
    // no header: the connection's address
    assertThat(limiter.tryAcquire(request("10.0.0.1", null))).isTrue();
    assertThat(limiter.tryAcquire(request("10.0.0.1", " "))).isFalse();
  }

  @Test
  void oldWindowsAreEvicted() {
    OnboardingRateLimiter limiter = new OnboardingRateLimiter(clock, 10, false);
    for (int i = 0; i < 100; i++) {
      limiter.tryAcquire("10.0.1." + i);
    }
    assertThat(limiter.trackedClients()).isEqualTo(100);

    clock.advance(Duration.ofMinutes(2));
    limiter.tryAcquire("10.0.2.1");

    assertThat(limiter.trackedClients()).isEqualTo(1);
  }

  @Test
  void memoryIsBoundedWhenTheTableIsFull() {
    OnboardingRateLimiter limiter = new OnboardingRateLimiter(clock, 10, false);
    for (int i = 0; i < OnboardingRateLimiter.MAX_TRACKED_CLIENTS; i++) {
      assertThat(limiter.tryAcquire("client-" + i)).isTrue();
    }

    assertThat(limiter.tryAcquire("one-more")).isFalse();
    assertThat(limiter.trackedClients()).isEqualTo(OnboardingRateLimiter.MAX_TRACKED_CLIENTS);
    // known clients are still served, and everything frees up in the next window
    assertThat(limiter.tryAcquire("client-0")).isTrue();
    clock.advance(Duration.ofMinutes(1));
    assertThat(limiter.tryAcquire("one-more")).isTrue();
  }

  @Test
  void retryAfterIsTheTimeLeftInTheWindow() {
    OnboardingRateLimiter limiter = new OnboardingRateLimiter(clock, 10, false);

    assertThat(limiter.retryAfterSeconds()).isEqualTo(50);
  }

  @Test
  void limitMustBePositive() {
    assertThatThrownBy(() -> new OnboardingRateLimiter(clock, 0, false))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
