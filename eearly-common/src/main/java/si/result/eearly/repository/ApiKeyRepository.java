package si.result.eearly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.result.eearly.domain.apikey.ApiKey;

import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
}
