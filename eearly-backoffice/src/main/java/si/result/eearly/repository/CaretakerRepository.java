package si.result.eearly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.result.eearly.domain.caretaker.Caretaker;

import java.util.UUID;

@Repository
public interface CaretakerRepository extends JpaRepository<Caretaker, UUID> {
}
