package si.result.eearly.domain.onboarding;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A one-time onboarding code. Only the hash of the code is stored, never the code itself, so
 * this class has no generated toString.
 */
@Entity
@Table(name = "onboarding_code")
@Getter
@Setter
@NoArgsConstructor
public class OnboardingCode {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "code_hash", nullable = false, length = 64, unique = true)
  private String codeHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public static OnboardingCode create(UUID userId, String codeHash, Instant now,
      Instant expiresAt) {
    OnboardingCode code = new OnboardingCode();
    code.setId(UUID.randomUUID());
    code.setUserId(userId);
    code.setCodeHash(codeHash);
    code.setCreatedAt(now);
    code.setExpiresAt(expiresAt);
    return code;
  }
}
