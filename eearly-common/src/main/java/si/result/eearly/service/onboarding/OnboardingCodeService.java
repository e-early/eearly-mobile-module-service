package si.result.eearly.service.onboarding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.onboarding.OnboardingCode;
import si.result.eearly.repository.OnboardingCodeRepository;

/**
 * Issues and redeems the one-time codes carried by onboarding links. Codes are 32 random bytes
 * (base64url, 43 characters); only their SHA-256 hash is stored. Codes and hashes are never logged.
 */
@Service
public class OnboardingCodeService {

  static final int CODE_BYTES = 32;
  static final int CODE_LENGTH = 43;
  private static final Pattern CODE_FORMAT = Pattern.compile("[A-Za-z0-9_-]{" + CODE_LENGTH + "}");
  private static final SecureRandom RANDOM = new SecureRandom();

  private final OnboardingCodeRepository repository;
  private final Clock clock;
  private final Duration validity;

  @Autowired
  public OnboardingCodeService(OnboardingCodeRepository repository, Clock clock,
      @Value("${onboarding.code.validity:P30D}") String validity) {
    this(repository, clock, DurationStyle.detectAndParse(validity));
  }

  public OnboardingCodeService(OnboardingCodeRepository repository, Clock clock,
      Duration validity) {
    if (validity == null || validity.isZero() || validity.isNegative()) {
      throw new IllegalArgumentException("onboarding.code.validity must be a positive duration");
    }
    this.repository = repository;
    this.clock = clock;
    this.validity = validity;
  }

  /** A new random code: 32 bytes from a {@link SecureRandom}, base64url without padding. */
  public static String generateCode() {
    byte[] bytes = new byte[CODE_BYTES];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  /** Lower-case hex SHA-256 of the code. */
  public static String hash(String code) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
          .digest(code.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }

  public Duration getValidity() {
    return validity;
  }

  /** Creates a code for the user, stores its hash and returns the code (shown only once). */
  @Transactional
  public String issue(UUID userId) {
    String code = generateCode();
    Instant now = clock.instant();
    repository.save(OnboardingCode.create(userId, hash(code), now, now.plus(validity)));
    return code;
  }

  /** Invalidates all unused codes of the user and issues a new one. */
  @Transactional
  public String replace(UUID userId) {
    repository.invalidateUnused(userId, clock.instant());
    return issue(userId);
  }

  /**
   * Redeems a code: returns the user id when the code exists, is unused and unexpired, and marks
   * it used in the same conditional statement, so a code can be redeemed only once even by
   * concurrent requests. Every other case (unknown, used, expired, malformed, a raw user id)
   * returns empty.
   */
  @Transactional
  public Optional<UUID> redeem(String code) {
    if (code == null || !CODE_FORMAT.matcher(code).matches()) {
      return Optional.empty();
    }
    String codeHash = hash(code);
    Optional<OnboardingCode> stored = repository.findByCodeHash(codeHash);
    if (stored.isEmpty()) {
      return Optional.empty();
    }
    UUID userId = stored.get().getUserId();
    if (repository.markUsed(codeHash, clock.instant()) != 1) {
      return Optional.empty();
    }
    return Optional.of(userId);
  }
}
