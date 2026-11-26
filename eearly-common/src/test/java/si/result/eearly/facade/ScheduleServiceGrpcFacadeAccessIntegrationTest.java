package si.result.eearly.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import si.result.eearly.domain.UserPrincipal;
import si.result.eearly.domain.enums.MeasurementMode;
import si.result.eearly.domain.enums.ScheduledMode;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.domain.schedule.ScheduleEntry;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.genproto.GetScheduleEntriesResponse;
import si.result.eearly.genproto.GetSchedulesResponse;
import si.result.eearly.mapper.ScheduleMapperImpl;
import si.result.eearly.mapper.TimeMapperImpl;
import si.result.eearly.repository.ScheduleRepository;
import si.result.eearly.service.ScheduleEntryServiceImpl;
import si.result.eearly.service.ScheduleServiceImpl;
import si.result.eearly.service.UserPrincipalService;

/** The schedule read RPCs return only the calling user's schedules, whatever filter is sent. */
@DataJpaTest(properties = {
    "spring.flyway.enabled=true",
    "spring.flyway.schemas=eearly_mobile",
    "spring.flyway.locations=classpath:db/migration/common",
    "spring.datasource.hikari.schema=eearly_mobile"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ScheduleServiceGrpcFacadeAccessIntegrationTest {

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @SpringBootConfiguration
  @EntityScan("si.result.eearly.domain")
  @EnableJpaRepositories(basePackageClasses = ScheduleRepository.class)
  @Import({ScheduleServiceGrpcFacade.class, ScheduleServiceImpl.class,
      ScheduleEntryServiceImpl.class, ScheduleMapperImpl.class, TimeMapperImpl.class})
  static class Config {}

  private static final UUID USER_ONE = UUID.fromString("0a000000-0000-0000-0000-00000000000a");
  private static final UUID USER_TWO = UUID.fromString("0b000000-0000-0000-0000-00000000000b");
  private static final UUID CARETAKER_ONE = UUID.fromString("ca000000-0000-0000-0000-00000000000a");
  private static final UUID CARETAKER_TWO = UUID.fromString("cb000000-0000-0000-0000-00000000000b");
  private static final UUID FACILITY = UUID.fromString("fa000000-0000-0000-0000-0000000000fa");

  @Autowired private ScheduleServiceGrpcFacade facade;
  @Autowired private EntityManager em;

  @MockitoBean private UserPrincipalService userPrincipalService;

  private Schedule scheduleOfUserOne;
  private Schedule scheduleOfUserTwo;
  private ScheduleEntry entryOfUserOne;
  private ScheduleEntry entryOfUserTwo;

  @BeforeEach
  void setUp() {
    // Facility and caretakers have no JPA entities in this service; schedules reference them.
    em.createNativeQuery("insert into facility (id, name, description) values (?1, 'Test facility', 'Test')")
        .setParameter(1, FACILITY).executeUpdate();
    for (UUID caretaker : List.of(CARETAKER_ONE, CARETAKER_TWO)) {
      em.createNativeQuery("insert into caretaker (id, name, facility_id) values (?1, 'Caretaker', ?2)")
          .setParameter(1, caretaker).setParameter(2, FACILITY).executeUpdate();
    }

    Profile userOne = Profile.createProfileFromId(USER_ONE.toString());
    Profile userTwo = Profile.createProfileFromId(USER_TWO.toString());
    userTwo.setNotificationDaySummary(true);
    em.persist(userOne);
    em.persist(userTwo);

    MeasurementType measurementType = em.createQuery(
            "select mt from MeasurementType mt order by mt.id", MeasurementType.class)
        .setMaxResults(1).getSingleResult();
    entryOfUserOne = entry(measurementType, "Entry one");
    entryOfUserTwo = entry(measurementType, "Entry two");
    scheduleOfUserOne = schedule(CARETAKER_ONE, "Schedule one", userOne, entryOfUserOne);
    scheduleOfUserTwo = schedule(CARETAKER_TWO, "Schedule two", userTwo, entryOfUserTwo);
    em.flush();
    em.clear();
  }

  @ParameterizedTest(name = "user {0}, filter [{1}]")
  @CsvSource(delimiter = '|', value = {
      "ONE | ''",
      "TWO | ''",
      "ONE | active:ISTRUE",
      "ONE | active:ISFALSE OR active:ISTRUE"})
  void getSchedules_returnsOnlyCallersSchedules(String user, String filter) {
    boolean isOne = "ONE".equals(user);
    callAs(isOne ? USER_ONE : USER_TWO);
    Schedule own = isOne ? scheduleOfUserOne : scheduleOfUserTwo;

    assertThat(scheduleIds(facade.getSchedules(filter))).containsExactly(own.getId().toString());
  }

  @Test
  void scheduleReads_requireAUserInTheToken() {
    when(userPrincipalService.getUserPrincipal()).thenReturn(null);

    assertThatThrownBy(() -> facade.getSchedules("")).isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> facade.getScheduleEntries(""))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void scheduleReads_requireAUserIdInThePrincipal() {
    when(userPrincipalService.getUserPrincipal()).thenReturn(new UserPrincipal());

    assertThatThrownBy(() -> facade.getSchedules("")).isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> facade.getScheduleEntries(""))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void filtersMayNotFollowRelationsToOtherUsersOfASharedSchedule() {
    Profile userOne = em.find(Profile.class, USER_ONE);
    Profile userTwo = em.find(Profile.class, USER_TWO);
    Schedule shared = Schedule.createSchedule(CARETAKER_ONE, UUID.randomUUID().toString(), "Shared",
        "Shared description", true, FACILITY);
    shared.setProfiles(List.of(userOne, userTwo));
    em.persist(shared);
    em.flush();
    em.clear();
    callAs(USER_ONE);

    assertThatThrownBy(() -> facade.getSchedules("profiles.notificationDaySummary:ISTRUE"))
        .isInstanceOf(ServiceInvalidRequestException.class);
    assertThatThrownBy(() -> facade.getScheduleEntries("schedules.name:EQ:Shared"))
        .isInstanceOf(ServiceInvalidRequestException.class);
    // Fields of the schedule itself still work.
    assertThat(scheduleIds(facade.getSchedules("name:EQ:Shared")))
        .containsExactly(shared.getId().toString());
  }

  @Test
  void getSchedules_rejectsFiltersOnRelatedRecords() {
    callAs(USER_ONE);

    assertThatThrownBy(() -> facade.getSchedules("profiles.userId:EQ:" + USER_TWO))
        .isInstanceOf(ServiceInvalidRequestException.class);
    assertThatThrownBy(() -> facade.getSchedules("profiles.notificationDaySummary:ISTRUE"))
        .isInstanceOf(ServiceInvalidRequestException.class);
  }

  @Test
  void getScheduleEntries_returnsOnlyCallersEntries() {
    callAs(USER_ONE);

    assertThat(entryIds(facade.getScheduleEntries("")))
        .containsExactly(entryOfUserOne.getId().toString());
    assertThat(entryIds(facade.getScheduleEntries("description:ISNULL OR description:ISNOTNULL")))
        .containsExactly(entryOfUserOne.getId().toString());
    assertThat(entryIds(facade.getScheduleEntries("description:EQ:Entry two"))).isEmpty();
  }

  private void callAs(UUID userId) {
    UserPrincipal principal = new UserPrincipal();
    principal.setId(userId.toString());
    when(userPrincipalService.getUserPrincipal()).thenReturn(principal);
  }

  private ScheduleEntry entry(MeasurementType measurementType, String description) {
    return ScheduleEntry.createScheduleEntry(null, measurementType, description,
        MeasurementMode.SINGLE, 0, OffsetDateTime.parse("2026-03-01T08:00:00Z"), 1,
        ScheduledMode.ONCE_A_DAY);
  }

  private Schedule schedule(UUID caretakerId, String name, Profile user, ScheduleEntry entry) {
    Schedule schedule = Schedule.createSchedule(caretakerId, UUID.randomUUID().toString(), name,
        name + " description", true, FACILITY);
    schedule.setProfiles(List.of(user));
    schedule.setScheduleEntries(new ArrayList<>(List.of(entry)));
    em.persist(schedule);
    return schedule;
  }

  private static List<String> scheduleIds(GetSchedulesResponse response) {
    return response.getSchedulesList().stream()
        .map(si.result.eearly.genproto.Schedule::getId)
        .toList();
  }

  private static List<String> entryIds(GetScheduleEntriesResponse response) {
    return response.getScheduleEntriesList().stream()
        .map(si.result.eearly.genproto.ScheduleEntry::getId)
        .toList();
  }
}
