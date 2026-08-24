package si.result.eearly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.result.eearly.domain.profile.Profile;

import java.util.UUID;

@Repository
public interface ProfileRepository  extends JpaRepository<Profile, UUID> {
}
