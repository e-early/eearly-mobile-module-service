package si.result.eearly.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import si.result.eearly.domain.schedule.ScheduleEntry;

import java.util.List;
import java.util.UUID;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, UUID> {
  List<ScheduleEntry> findAll(Specification<ScheduleEntry> specification);

}
