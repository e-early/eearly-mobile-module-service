package si.result.eearly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.result.eearly.domain.token.MobileToken;

import java.util.List;
import java.util.UUID;

@Repository
public interface MobileTokenRepository extends JpaRepository<MobileToken, String> {

    List<MobileToken> findAllByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    void deleteByToken(String token);

    void deleteByTokenAndUserId(String token, UUID userId);

    boolean existsMobileTokenByTokenAndUserId(String token, UUID userId);
}
