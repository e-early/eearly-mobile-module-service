package si.result.eearly.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import si.result.eearly.domain.onboarding.OnboardingCode;

@Repository
public interface OnboardingCodeRepository extends JpaRepository<OnboardingCode, UUID> {

  Optional<OnboardingCode> findByCodeHash(String codeHash);

  /**
   * Marks the code as used if it is still unused and unexpired, in one conditional statement, so
   * of two concurrent redemptions only one gets a row count of 1.
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update OnboardingCode c set c.usedAt = :now "
      + "where c.codeHash = :codeHash and c.usedAt is null and c.expiresAt > :now")
  int markUsed(@Param("codeHash") String codeHash, @Param("now") Instant now);

  /** Invalidates every unused code of the user. */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update OnboardingCode c set c.usedAt = :now where c.userId = :userId and c.usedAt is null")
  int invalidateUnused(@Param("userId") UUID userId, @Param("now") Instant now);
}
