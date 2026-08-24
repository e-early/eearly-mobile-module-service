package si.result.eearly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.result.eearly.domain.facility.Facility;

import java.util.UUID;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, UUID> {
}
