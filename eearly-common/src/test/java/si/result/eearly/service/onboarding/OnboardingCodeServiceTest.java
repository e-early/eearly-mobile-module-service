package si.result.eearly.service.onboarding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import si.result.eearly.domain.onboarding.OnboardingCode;
import si.result.eearly.repository.OnboardingCodeRepository;

class OnboardingCodeServiceTest {

  private static final Instant NOW = Instant.parse("2026-03-01T10:00:00Z");
  private static final Duration VALIDITY = Duration.ofDays(30);

  private final OnboardingCodeRepository repository = mock(OnboardingCodeRepository.class);
  private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
  private OnboardingCodeService service;

  @BeforeEach
  void setUp() {
    service = new OnboardingCodeService(repository, clock, VALIDITY);
  }

  @Test
  void generatedCodeIs43CharactersOfBase64UrlWithoutPadding() {
    for (int i = 0; i < 200; i++) {
      assertThat(OnboardingCodeService.generateCode()).matches("[A-Za-z0-9_-]{43}");
    }
  }

  @Test
  void generatedCodesAreUnique() {
    Set<String> codes = new HashSet<>();
    for (int i = 0; i < 10_000; i++) {
      codes.add(OnboardingCodeService.generateCode());
    }
    assertThat(codes).hasSize(10_000);
  }

  @Test
  void hashIsLowerCaseHexSha256() {
    assertThat(OnboardingCodeService.hash("abc"))
        .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    assertThat(OnboardingCodeService.hash(OnboardingCodeService.generateCode())).hasSize(64)
        .matches("[0-9a-f]{64}");
  }

  @Test
  void issueStoresOnlyTheHashAndTheConfiguredValidity() {
    UUID userId = UUID.randomUUID();

    String code = service.issue(userId);

    ArgumentCaptor<OnboardingCode> saved = ArgumentCaptor.forClass(OnboardingCode.class);
    verify(repository).save(saved.capture());
    OnboardingCode stored = saved.getValue();
    assertThat(stored.getUserId()).isEqualTo(userId);
    assertThat(stored.getCodeHash()).isEqualTo(OnboardingCodeService.hash(code))
        .isNotEqualTo(code);
    assertThat(stored.getCreatedAt()).isEqualTo(NOW);
    assertThat(stored.getExpiresAt()).isEqualTo(NOW.plus(VALIDITY));
    assertThat(stored.getUsedAt()).isNull();
    assertThat(stored.getId()).isNotNull();
  }

  @Test
  void validityIsReadFromIsoAndSimpleDurations() {
    assertThat(new OnboardingCodeService(repository, clock, "P30D").getValidity())
        .isEqualTo(Duration.ofDays(30));
    assertThat(new OnboardingCodeService(repository, clock, "12h").getValidity())
        .isEqualTo(Duration.ofHours(12));
  }

  @Test
  void validityMustBePositive() {
    assertThatThrownBy(() -> new OnboardingCodeService(repository, clock, Duration.ZERO))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new OnboardingCodeService(repository, clock, Duration.ofDays(-1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void replaceInvalidatesUnusedCodesBeforeIssuingANewOne() {
    UUID userId = UUID.randomUUID();

    service.replace(userId);

    InOrder order = inOrder(repository);
    order.verify(repository).invalidateUnused(userId, NOW);
    order.verify(repository).save(any(OnboardingCode.class));
  }

  @Test
  void redeemReturnsTheUserWhenTheCodeWasMarkedUsed() {
    UUID userId = UUID.randomUUID();
    String code = OnboardingCodeService.generateCode();
    String hash = OnboardingCodeService.hash(code);
    when(repository.findByCodeHash(hash)).thenReturn(Optional.of(stored(userId, hash)));
    when(repository.markUsed(hash, NOW)).thenReturn(1);

    assertThat(service.redeem(code)).contains(userId);
  }

  @Test
  void redeemIsEmptyWhenTheConditionalUpdateMatchesNothing() {
    // used or expired, or taken by a concurrent request between the lookup and the update
    UUID userId = UUID.randomUUID();
    String code = OnboardingCodeService.generateCode();
    String hash = OnboardingCodeService.hash(code);
    when(repository.findByCodeHash(hash)).thenReturn(Optional.of(stored(userId, hash)));
    when(repository.markUsed(hash, NOW)).thenReturn(0);

    assertThat(service.redeem(code)).isEmpty();
  }

  @Test
  void redeemIsEmptyForAnUnknownCode() {
    String code = OnboardingCodeService.generateCode();
    when(repository.findByCodeHash(OnboardingCodeService.hash(code))).thenReturn(Optional.empty());

    assertThat(service.redeem(code)).isEmpty();
    verify(repository, never()).markUsed(anyString(), any());
  }

  @Test
  void redeemIsEmptyForRawUserIdsAndMalformedCodesWithoutTouchingTheDatabase() {
    assertThat(service.redeem(UUID.randomUUID().toString())).isEmpty();
    assertThat(service.redeem("configuration")).isEmpty();
    assertThat(service.redeem("")).isEmpty();
    assertThat(service.redeem(null)).isEmpty();
    assertThat(service.redeem("short")).isEmpty();
    assertThat(service.redeem(OnboardingCodeService.generateCode() + "x")).isEmpty();
    assertThat(service.redeem("!".repeat(43))).isEmpty();
    assertThat(service.redeem("a".repeat(42) + "=")).isEmpty();

    verifyNoInteractions(repository);
  }

  private static OnboardingCode stored(UUID userId, String hash) {
    return OnboardingCode.create(userId, hash, NOW.minusSeconds(60), NOW.plus(VALIDITY));
  }
}
