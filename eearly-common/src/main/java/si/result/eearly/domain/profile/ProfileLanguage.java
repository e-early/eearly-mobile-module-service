package si.result.eearly.domain.profile;

import java.util.Set;

/**
 * Languages a patient's app and push notifications can be in, as ISO 639-1 codes. Must match the
 * admin service's {@code PatientLanguage}.
 */
public final class ProfileLanguage {

  public static final String SLOVENIAN = "sl";
  public static final String ENGLISH = "en";

  /** All current customers are Slovenian. */
  public static final String DEFAULT = SLOVENIAN;

  public static final Set<String> SUPPORTED = Set.of(SLOVENIAN, ENGLISH);

  private ProfileLanguage() {}

  public static boolean isSupported(String language) {
    return language != null && SUPPORTED.contains(language);
  }
}
