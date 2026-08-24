package si.result.eearly.domain.schedule;

import io.netty.util.internal.StringUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import si.result.eearly.domain.profile.Profile;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "schedule")
@Getter
@Setter
public class Schedule {
  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "active", nullable = false)
  private boolean active;

  @Column(name ="facility_id")
  private UUID facilityId;

  @Column(name = "caretaker_id")
  private UUID caretakerId;

  @ManyToMany(fetch = FetchType.EAGER,
          cascade = {
                  CascadeType.PERSIST,
                  CascadeType.MERGE,
                  CascadeType.REMOVE
          })
  @JoinTable(
          name = "schedule_entry_schedule",
          joinColumns = @JoinColumn(name = "schedule_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "schedule_entry_id", referencedColumnName = "id")
  )
  private List<ScheduleEntry> scheduleEntries;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
          name = "schedule_user",
          joinColumns = @JoinColumn(name = "schedule_id", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id")
  )
  private List<Profile> profiles;

  public static Schedule createSchedule(UUID caretakerId, String id, String name, String description, boolean active, UUID facilityId) {

    Schedule schedule = new Schedule();
    if (!StringUtil.isNullOrEmpty(id)) {
      schedule.id = UUID.fromString(id);
    }
    schedule.caretakerId = caretakerId;
    schedule.facilityId = facilityId;
    schedule.name = name;
    schedule.description = description;
    schedule.active = active;
    return schedule;
  }

  public static Schedule createScheduleFromUpsertCommand(UpsertScheduleCommand command, UUID facilityId) {
    Schedule scheduleToUpsert = createSchedule(
            UUID.fromString(command.caretakerId()),
            command.id(),
            command.name(),
            command.description(),
            command.active(),
            facilityId);
    return scheduleToUpsert;
  }

  public void addScheduleEntry(ScheduleEntry scheduleEntry) {
    this.scheduleEntries.add(scheduleEntry);
  }

  public void removeScheduleEntry(ScheduleEntry scheduleEntry) {
    this.scheduleEntries.remove(scheduleEntry);
  }

  public ScheduleEntry getScheduleEntry(String scheduleEntryId) {
    for (ScheduleEntry scheduleEntry : this.scheduleEntries) {
      if (scheduleEntry.getId().equals(UUID.fromString(scheduleEntryId))) {
        return scheduleEntry;
      }
    }
    return null;
  }
}
