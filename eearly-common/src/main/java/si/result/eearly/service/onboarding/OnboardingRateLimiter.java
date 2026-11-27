package si.result.eearly.service.onboarding;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * In-memory fixed-window rate limiter for the anonymous onboarding endpoints, per client address.
 *
 * <p>The client address is {@code request.getRemoteAddr()}; the first hop of
 * {@code X-Forwarded-For} is used only when {@code onboarding.rate-limit.trust-forwarded-for} is
 * true (behind a proxy that sets the header). Memory is bounded: windows that have ended are
 * evicted once per window, and when the table is still full new addresses are refused until the
 * next window.
 */
@Component
public class OnboardingRateLimiter {

  static final long WINDOW_MILLIS = 60_000L;
  static final int MAX_TRACKED_CLIENTS = 50_000;

  private record Counter(long window, int count) {}

  private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
  private final Clock clock;
  private final int requestsPerMinute;
  private final boolean trustForwardedFor;
  private volatile long lastEvictedWindow = -1;

  @Autowired
  public OnboardingRateLimiter(Clock clock,
      @Value("${onboarding.rate-limit.requests-per-minute:10}") int requestsPerMinute,
      @Value("${onboarding.rate-limit.trust-forwarded-for:false}") boolean trustForwardedFor) {
    if (requestsPerMinute < 1) {
      throw new IllegalArgumentException(
          "onboarding.rate-limit.requests-per-minute must be at least 1");
    }
    this.clock = clock;
    this.requestsPerMinute = requestsPerMinute;
    this.trustForwardedFor = trustForwardedFor;
  }

  /** Counts the request; returns false when the client exceeded its limit for this window. */
  public boolean tryAcquire(HttpServletRequest request) {
    return tryAcquire(clientAddress(request));
  }

  boolean tryAcquire(String client) {
    long window = clock.millis() / WINDOW_MILLIS;
    evictOldWindows(window);

    if (counters.size() >= MAX_TRACKED_CLIENTS && !counters.containsKey(client)) {
      return false;
    }
    Counter counter = counters.compute(client,
        (key, current) -> current == null || current.window() != window
            ? new Counter(window, 1)
            : new Counter(window, current.count() + 1));
    return counter.count() <= requestsPerMinute;
  }

  /** Seconds until the current window ends. */
  public long retryAfterSeconds() {
    long millisIntoWindow = clock.millis() % WINDOW_MILLIS;
    return Math.max(1, (WINDOW_MILLIS - millisIntoWindow + 999) / 1000);
  }

  int trackedClients() {
    return counters.size();
  }

  private void evictOldWindows(long window) {
    if (lastEvictedWindow != window) {
      lastEvictedWindow = window;
      counters.values().removeIf(counter -> counter.window() < window);
    }
  }

  private String clientAddress(HttpServletRequest request) {
    if (trustForwardedFor) {
      String forwarded = request.getHeader("X-Forwarded-For");
      if (forwarded != null && !forwarded.isBlank()) {
        String first = forwarded.split(",", 2)[0].trim();
        if (!first.isEmpty()) {
          return first;
        }
      }
    }
    return request.getRemoteAddr();
  }
}
