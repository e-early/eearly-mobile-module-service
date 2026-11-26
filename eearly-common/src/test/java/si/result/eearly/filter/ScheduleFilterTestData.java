package si.result.eearly.filter;

import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import si.result.eearly.domain.enums.MeasurementMode;
import si.result.eearly.domain.enums.ScheduledMode;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.domain.schedule.ScheduleEntry;

/**
 * Fixed schedules and schedule entries for the filter characterization tests. Facility and
 * caretakers have no JPA entities in this service, so they are inserted with SQL.
 */
final class ScheduleFilterTestData {

  static final UUID FACILITY = UUID.fromString("fa000000-0000-0000-0000-0000000000fa");
  static final UUID CARETAKER_1 = UUID.fromString("c1000000-0000-0000-0000-0000000000c1");
  static final UUID CARETAKER_2 = UUID.fromString("c2000000-0000-0000-0000-0000000000c2");
  static final UUID PATIENT_1 = UUID.fromString("a1000000-0000-0000-0000-0000000000a1");

  static final UUID ALPHA = UUID.fromString("5a000000-0000-0000-0000-0000000000a1");
  static final UUID BETA = UUID.fromString("5b000000-0000-0000-0000-0000000000b2");
  static final UUID GAMMA = UUID.fromString("5c000000-0000-0000-0000-0000000000c3");

  static final UUID MORNING = UUID.fromString("e1000000-0000-0000-0000-0000000000e1");
  static final UUID EVENING = UUID.fromString("e2000000-0000-0000-0000-0000000000e2");
  static final UUID WEEKLY = UUID.fromString("e3000000-0000-0000-0000-0000000000e3");

  private ScheduleFilterTestData() {}

  /**
   * Alpha (active, caretaker 1, patient 1, entries Morning + Evening), Beta (active, caretaker 2,
   * entry Weekly), Gamma (inactive, caretaker 1, no entries).
   */
  static void insert(EntityManager em) {
    em.createNativeQuery(
            "insert into facility (id, name, description) values (?1, 'Test facility', 'Test')")
        .setParameter(1, FACILITY).executeUpdate();
    for (UUID caretaker : List.of(CARETAKER_1, CARETAKER_2)) {
      em.createNativeQuery("insert into caretaker (id, name, facility_id) values (?1, 'Caretaker', ?2)")
          .setParameter(1, caretaker).setParameter(2, FACILITY).executeUpdate();
    }

    Profile patient = Profile.createProfileFromId(PATIENT_1.toString());
    em.persist(patient);

    MeasurementType measurementType = em.createQuery(
            "select mt from MeasurementType mt order by mt.id", MeasurementType.class)
        .setMaxResults(1).getSingleResult();

    ScheduleEntry morning = ScheduleEntry.createScheduleEntry(MORNING.toString(), measurementType,
        "Morning measurement", MeasurementMode.SINGLE, 0,
        OffsetDateTime.parse("2026-03-01T08:00:00Z"), 1, ScheduledMode.ONCE_A_DAY);
    ScheduleEntry evening = ScheduleEntry.createScheduleEntry(EVENING.toString(), measurementType,
        "Evening batch", MeasurementMode.BATCH, 5,
        OffsetDateTime.parse("2026-03-01T20:00:00Z"), 3, ScheduledMode.AT_SPECIFIC_HOUR);
    ScheduleEntry weekly = ScheduleEntry.createScheduleEntry(WEEKLY.toString(), measurementType,
        null, MeasurementMode.CONTINUOUS, 60,
        OffsetDateTime.parse("2026-04-15T12:00:00Z"), 1, ScheduledMode.ONCE_A_DAY);

    schedule(em, ALPHA, "Alpha", "First schedule", true, CARETAKER_1,
        List.of(patient), List.of(morning, evening));
    schedule(em, BETA, "Beta", "Second schedule", true, CARETAKER_2,
        List.of(), List.of(weekly));
    schedule(em, GAMMA, "Gamma", "Inactive schedule", false, CARETAKER_1,
        List.of(), List.of());
    em.flush();
    em.clear();
  }

  private static void schedule(EntityManager em, UUID id, String name, String description,
      boolean active, UUID caretaker, List<Profile> patients, List<ScheduleEntry> entries) {
    Schedule schedule = Schedule.createSchedule(caretaker, id.toString(), name, description, active,
        FACILITY);
    schedule.setProfiles(new ArrayList<>(patients));
    schedule.setScheduleEntries(new ArrayList<>(entries));
    em.persist(schedule);
  }
}
