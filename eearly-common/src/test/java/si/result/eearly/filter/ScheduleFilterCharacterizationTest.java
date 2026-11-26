package si.result.eearly.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static si.result.eearly.filter.ScheduleFilterTestData.ALPHA;
import static si.result.eearly.filter.ScheduleFilterTestData.BETA;
import static si.result.eearly.filter.ScheduleFilterTestData.EVENING;
import static si.result.eearly.filter.ScheduleFilterTestData.GAMMA;
import static si.result.eearly.filter.ScheduleFilterTestData.MORNING;
import static si.result.eearly.filter.ScheduleFilterTestData.WEEKLY;

import jakarta.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.domain.schedule.ScheduleEntry;
import si.result.eearly.repository.ScheduleEntryRepository;
import si.result.eearly.repository.ScheduleRepository;
import si.result.r3.filter.config.RestFilterGenerator;

/**
 * Characterization of the schedule filter syntax that mobile clients send in the gRPC
 * {@code filter} fields (ScheduleService and BackofficeScheduleService).
 *
 * <p>Tests named {@code contract*} describe behaviour clients rely on; any replacement of the
 * filter implementation must keep them passing unchanged. Tests named {@code currentDefect*}
 * record known defects of the current implementation; a replacement is expected to change them
 * deliberately, and those tests are then updated in the same change.
 *
 * <p>Only {@link #translator} refers to the implementation, so the same tests can run against a
 * replacement.
 */
@DataJpaTest(properties = {
    "spring.flyway.enabled=true",
    "spring.flyway.schemas=eearly_mobile",
    "spring.flyway.locations=classpath:db/migration/common",
    "spring.datasource.hikari.schema=eearly_mobile"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ScheduleFilterCharacterizationTest {

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @SpringBootConfiguration
  @EntityScan("si.result.eearly.domain")
  @EnableJpaRepositories(basePackageClasses = ScheduleRepository.class)
  static class Config {}

  /** Translates a filter string into a JPA specification. The only implementation-specific part. */
  interface FilterTranslator {
    <T> Specification<T> toSpecification(String filter, Class<T> type);
  }

  private final FilterTranslator translator = new FilterTranslator() {
    private final RestFilterGenerator generator = new RestFilterGenerator(new HashMap<>());

    @Override
    public <T> Specification<T> toSpecification(String filter, Class<T> type) {
      return generator.parse(filter).toSpecification(type);
    }
  };

  @Autowired private EntityManager em;
  @Autowired private ScheduleRepository schedules;
  @Autowired private ScheduleEntryRepository entries;

  @BeforeEach
  void setUp() {
    ScheduleFilterTestData.insert(em);
  }

  private Set<UUID> schedulesMatching(String filter) {
    return schedules.findAll(translator.toSpecification(filter, Schedule.class)).stream()
        .map(Schedule::getId).collect(Collectors.toSet());
  }

  private Set<UUID> entriesMatching(String filter) {
    return entries.findAll(translator.toSpecification(filter, ScheduleEntry.class)).stream()
        .map(ScheduleEntry::getId).collect(Collectors.toSet());
  }

  static Stream<Arguments> scheduleFilters() {
    return Stream.of(
        arguments("", Set.of(ALPHA, BETA, GAMMA)),
        arguments(" ", Set.of(ALPHA, BETA, GAMMA)),
        arguments("active:ISTRUE", Set.of(ALPHA, BETA)),
        arguments("active:ISFALSE", Set.of(GAMMA)),
        arguments("active:EQ:true", Set.of(ALPHA, BETA)),
        arguments("name:EQ:Alpha", Set.of(ALPHA)),
        arguments("name:EQ:alpha", Set.of()),
        arguments("name:EQ_IC:alpha", Set.of(ALPHA)),
        arguments("name:NEQ:Alpha", Set.of(BETA, GAMMA)),
        arguments("name:NEQ_IC:alpha", Set.of(BETA, GAMMA)),
        arguments("name:LIKE:lph", Set.of(ALPHA)),
        arguments("name:LIKE:%lph%", Set.of(ALPHA)),
        arguments("name:LIKE_IC:ALP", Set.of(ALPHA)),
        arguments("name:IN:[Alpha,Beta]", Set.of(ALPHA, BETA)),
        arguments("name:GT:Beta", Set.of(GAMMA)),
        arguments("description:ISNOTNULL", Set.of(ALPHA, BETA, GAMMA)),
        arguments("name:EQ:'Alpha'", Set.of(ALPHA)),
        arguments("name:EQ:\"Alpha\"", Set.of(ALPHA)),
        arguments("name:EQ:Alpha AND active:ISTRUE", Set.of(ALPHA)),
        arguments("name:EQ:Alpha OR name:EQ:Beta", Set.of(ALPHA, BETA)),
        arguments("NOT name:EQ:Alpha", Set.of(BETA, GAMMA)),
        arguments("(name:EQ:Alpha OR name:EQ:Gamma) AND active:ISTRUE", Set.of(ALPHA)));
  }

  @ParameterizedTest(name = "Schedule [{0}]")
  @MethodSource("scheduleFilters")
  void contractScheduleFilter(String filter, Set<UUID> expected) {
    assertThat(schedulesMatching(filter)).containsExactlyInAnyOrderElementsOf(expected);
  }

  static Stream<Arguments> entryFilters() {
    return Stream.of(
        arguments("", Set.of(MORNING, EVENING, WEEKLY)),
        arguments("measurementsInBatch:GT:1", Set.of(EVENING)),
        arguments("measurementsInBatch:EQ:3", Set.of(EVENING)),
        arguments("intervalMinutes:GTE:5", Set.of(EVENING, WEEKLY)),
        arguments("intervalMinutes:LT:60", Set.of(MORNING, EVENING)),
        arguments("scheduledAt:GT:2026-03-01T12:00:00Z", Set.of(EVENING, WEEKLY)),
        arguments("scheduledAt:GTE:2026-03-01T08:00:00Z", Set.of(MORNING, EVENING, WEEKLY)),
        arguments("scheduledAt:LT:2026-04-01T00:00:00+02:00", Set.of(MORNING, EVENING)),
        arguments("scheduledMode:EQ:ONCE_A_DAY", Set.of(MORNING, WEEKLY)),
        arguments("measurementMode:EQ:single", Set.of()),
        arguments("measurementMode:IN:[SINGLE,BATCH]", Set.of(MORNING, EVENING)),
        arguments("description:LIKE_IC:MORN", Set.of(MORNING)),
        arguments("description:ISNULL", Set.of(WEEKLY)));
  }

  @ParameterizedTest(name = "ScheduleEntry [{0}]")
  @MethodSource("entryFilters")
  void contractEntryFilter(String filter, Set<UUID> expected) {
    assertThat(entriesMatching(filter)).containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void contractUnknownAttributeIsRejected() {
    assertThatThrownBy(() -> schedulesMatching("unknownField:EQ:x"))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void currentDefectNinIgnoresItsList() {
    assertThat(schedulesMatching("name:NIN:[Alpha]")).containsExactlyInAnyOrder(ALPHA, BETA, GAMMA);
  }

  @Test
  void currentDefectSemicolonSilentlyMatchesNothing() {
    assertThat(schedulesMatching("name:EQ:Alpha;active:ISTRUE")).isEmpty();
    assertThat(schedulesMatching("name:EQ:Alpha;")).isEmpty();
  }

  @Test
  void currentDefectSpaceSeparatedListSilentlyMatchesNothing() {
    assertThat(schedulesMatching("name:IN:[Alpha Beta]")).isEmpty();
  }

  @Test
  void currentDefectEmptyValueMatchesNothing() {
    assertThat(schedulesMatching("name:EQ:")).isEmpty();
  }

  @Test
  void currentDefectUuidComparisonFailsWithServerError() {
    assertThatThrownBy(() -> schedulesMatching("caretakerId:EQ:" + ScheduleFilterTestData.CARETAKER_1))
        .hasRootCauseMessage("Cannot compare left expression of type 'java.util.UUID' with right"
            + " expression of type 'java.lang.String'");
  }

  @Test
  void currentDefectDateWithoutTimeFails() {
    assertThatThrownBy(() -> entriesMatching("scheduledAt:GT:2026-03-01"))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void currentDefectMalformedFiltersCrashWithNullPointerException() {
    for (String filter : List.of("garbage", "name:BOGUS:x", "name:IN_IC:[alpha,beta]")) {
      assertThatThrownBy(() -> schedulesMatching(filter))
          .as(filter).isInstanceOf(NullPointerException.class);
    }
  }

  /** S3: filters can follow relations into other entities. Replacement: per-field allowlist. */
  @Test
  void currentDefectFiltersCanTraverseRelations() {
    assertThat(schedulesMatching("scheduleEntries.description:EQ:Morning measurement"))
        .containsExactly(ALPHA);
    assertThat(schedulesMatching("scheduleEntries.measurementsInBatch:GT:2"))
        .containsExactly(ALPHA);
    assertThat(entriesMatching("schedules.name:EQ:Alpha"))
        .containsExactlyInAnyOrder(MORNING, EVENING);
  }
}
