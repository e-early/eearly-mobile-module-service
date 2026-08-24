package si.result.eearly.domain.schedule;

import ch.qos.logback.core.util.StringUtil;
import jakarta.persistence.*;
import lombok.Getter;
import si.result.eearly.domain.enums.MeasurementMode;
import si.result.eearly.domain.enums.ScheduledMode;
import si.result.eearly.domain.measurement.MeasurementType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "schedule_entry")
@Getter
public class ScheduleEntry {
  @Id
  @Column(name = "id")
  private UUID id;

  @ManyToMany(mappedBy = "scheduleEntries")
  private List<Schedule> schedules;

  @ManyToOne
  @JoinColumn(name = "measurement_type_id", nullable = false)
  private MeasurementType measurementType;

  @Column(name = "scheduled_at", nullable = false)
  private OffsetDateTime scheduledAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "scheduled_mode", nullable = false)
  private ScheduledMode scheduledMode;

  @Enumerated(EnumType.STRING)
  @Column(name = "measurement_mode", nullable = false)
  private MeasurementMode measurementMode;

  @Column(name = "description")
  private String description;

  @Column(name = "measurements_in_batch")
  private Integer measurementsInBatch;

  @Column(name = "interval_minutes")
  private Integer intervalMinutes;

  public static ScheduleEntry createScheduleEntry(String id, MeasurementType measurementType, String description,
                                                  MeasurementMode measurementMode, int intervalMinutes,
                                                  OffsetDateTime scheduledAt, int measurementsInBatch,
                                                  ScheduledMode scheduledMode) {

    ScheduleEntry entry = new ScheduleEntry();
    if (!StringUtil.isNullOrEmpty(id)) {
      entry.id = UUID.fromString(id);
    } else {
      entry.id = UUID.randomUUID();
    }
    entry.description = description;
    entry.measurementMode = measurementMode;
    entry.intervalMinutes = intervalMinutes;
    entry.scheduledAt = scheduledAt;
    entry.measurementsInBatch = measurementsInBatch;
    entry.measurementType = measurementType;
    entry.scheduledMode = scheduledMode;
    return entry;
  }
}
