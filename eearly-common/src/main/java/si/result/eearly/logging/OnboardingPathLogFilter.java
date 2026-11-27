package si.result.eearly.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

/**
 * Drops log events that contain a request path under {@code /api/v1/onboarding/} with a one-time
 * code in it (the path segment after the prefix). Request lines such as "Securing GET /path" are
 * written by framework loggers when verbose levels are switched on; the code must not reach a log
 * file. Paths of the endpoints that carry no code ({@code create-user},
 * {@code users/{id}/onboarding-link}) pass.
 *
 * <p>Registered in {@code logback-spring.xml} as a turbo filter, so it applies to every appender.
 */
public class OnboardingPathLogFilter extends TurboFilter {

  static final String PREFIX = "/api/v1/onboarding/";

  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String format,
      Object[] params, Throwable t) {
    if (containsCode(format) || containsCode(params) || (t != null && containsCode(t.getMessage()))) {
      return FilterReply.DENY;
    }
    return FilterReply.NEUTRAL;
  }

  private static boolean containsCode(Object[] params) {
    if (params == null) {
      return false;
    }
    for (Object param : params) {
      if (param instanceof CharSequence text && containsCode(text.toString())) {
        return true;
      }
    }
    return false;
  }

  static boolean containsCode(String text) {
    if (text == null) {
      return false;
    }
    int from = 0;
    while (true) {
      int at = text.indexOf(PREFIX, from);
      if (at < 0) {
        return false;
      }
      int start = at + PREFIX.length();
      int end = start;
      while (end < text.length() && isPathChar(text.charAt(end))) {
        end++;
      }
      // Everything after the prefix is masked, except the two endpoints that carry no code.
      String path = text.substring(start, end);
      if (!path.isEmpty() && !path.equals("create-user")
          && !path.matches("users/[^/]+/onboarding-link")) {
        return true;
      }
      from = start;
    }
  }

  private static boolean isPathChar(char c) {
    return !(c == '?' || c == '#' || c == '"' || c == '\'' || c == ']' || c == ','
        || c == ')' || Character.isWhitespace(c));
  }
}
