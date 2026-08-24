package si.result.eearly.service;

import org.springframework.data.jpa.domain.Specification;
import si.result.eearly.domain.schedule.ScheduleEntry;

import java.util.List;

public interface ScheduleEntryService {
  List<ScheduleEntry> getScheduleEntries(Specification<ScheduleEntry> specification);

}
