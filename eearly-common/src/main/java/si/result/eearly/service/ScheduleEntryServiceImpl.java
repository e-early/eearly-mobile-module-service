package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.schedule.ScheduleEntry;
import si.result.eearly.repository.ScheduleEntryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleEntryServiceImpl implements  ScheduleEntryService {
  private final ScheduleEntryRepository scheduleEntryRepository;

  @Override
  public List<ScheduleEntry> getScheduleEntries(Specification<ScheduleEntry> specification) {
    return scheduleEntryRepository.findAll(specification);
  }
}
