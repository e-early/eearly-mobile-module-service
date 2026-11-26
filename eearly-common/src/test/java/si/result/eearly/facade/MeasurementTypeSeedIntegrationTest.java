package si.result.eearly.facade;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import si.result.eearly.domain.enums.MeasurementTypeEnum;
import si.result.eearly.genproto.GetMeasurementTypesResponse;
import si.result.eearly.mapper.MeasurementTypeMapperImpl;
import si.result.eearly.repository.MeasurementTypeRepository;
import si.result.eearly.service.MeasurementTypeServiceImpl;

/** Migrating an empty database seeds a measurement_type row for every real MeasurementTypeEnum value. */
@DataJpaTest(properties = {
    "spring.flyway.enabled=true",
    "spring.flyway.schemas=eearly_mobile",
    "spring.flyway.locations=classpath:db/migration/common",
    "spring.datasource.hikari.schema=eearly_mobile"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class MeasurementTypeSeedIntegrationTest {

  private static final String SEED_MIGRATION =
      "db/migration/common/V2__seed_additional_measurement_types.sql";

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @SpringBootConfiguration
  @EntityScan("si.result.eearly.domain")
  @EnableJpaRepositories(basePackageClasses = MeasurementTypeRepository.class)
  @Import({MeasurementTypeServiceGrpcFacade.class, MeasurementTypeServiceImpl.class,
      MeasurementTypeMapperImpl.class})
  static class Config {}

  @Autowired private MeasurementTypeServiceGrpcFacade facade;
  @Autowired private JdbcTemplate jdbc;

  private static Set<String> expectedTypeNames() {
    return Arrays.stream(MeasurementTypeEnum.values())
        .filter(value -> value != MeasurementTypeEnum.UNKNOWN_MEASUREMENT_TYPE)
        .map(Enum::name)
        .collect(Collectors.toSet());
  }

  private Set<String> storedTypeNames() {
    return Set.copyOf(jdbc.queryForList(
        "select distinct measurement_type from measurement_type", String.class));
  }

  private int rowCount() {
    return jdbc.queryForObject("select count(*) from measurement_type", Integer.class);
  }

  private void runSeedMigrationAgain() throws IOException {
    String sql = new ClassPathResource(SEED_MIGRATION).getContentAsString(StandardCharsets.UTF_8);
    jdbc.execute(sql);
  }

  @Test
  void everyEnumValueHasAMeasurementTypeRow() {
    assertThat(storedTypeNames()).containsExactlyInAnyOrderElementsOf(expectedTypeNames());
  }

  @Test
  void everyRowIsCompleteAndSane() {
    List<MeasurementTypeRowCheck> rows = jdbc.query(
        "select measurement_type, unit, min_value, max_value, ehr_observation_id from measurement_type",
        (rs, i) -> new MeasurementTypeRowCheck(rs.getString(1), rs.getString(2), rs.getDouble(3),
            rs.getDouble(4), rs.getString(5)));

    assertThat(rows).isNotEmpty().allSatisfy(row -> {
      assertThat(row.unit()).as(row.type()).isNotNull();
      assertThat(row.min()).as(row.type()).isLessThan(row.max());
      assertThat(row.observationId()).as(row.type()).startsWith("openEHR-EHR-OBSERVATION.");
    });
  }

  @Test
  void getMeasurementTypesReturnsEveryType() {
    GetMeasurementTypesResponse response = facade.getMeasurementTypes();

    Set<String> returned = response.getMeasurementTypesList().stream()
        .map(type -> type.getMeasurementType().name())
        .collect(Collectors.toSet());
    assertThat(returned).containsExactlyInAnyOrderElementsOf(expectedTypeNames());
    assertThat(response.getMeasurementTypesList())
        .allSatisfy(type -> assertThat(UUID.fromString(type.getMeasurementTypeId())).isNotNull());
  }

  @Test
  void newTypesKeepTheirFixedIdsAndUnits() {
    assertThat(jdbc.queryForObject(
        "select unit from measurement_type where id = 'bd9b1917-3dd4-4f3a-8a03-28705b980627'",
        String.class)).isEqualTo("kg");
    assertThat(jdbc.queryForObject(
        "select measurement_type from measurement_type where id = 'd54dbf39-7158-4031-adfe-0f23ee093d9b'",
        String.class)).isEqualTo("BODY_IMPEDANCE");
  }

  @Test
  void seedMigrationIsIdempotent() throws IOException {
    int before = rowCount();

    runSeedMigrationAgain();
    runSeedMigrationAgain();

    assertThat(rowCount()).isEqualTo(before);
  }

  @Test
  void seedMigrationDoesNotDuplicateATypeThatExistsUnderAnotherId() throws IOException {
    jdbc.update("update measurement_type set id = ?::uuid where measurement_type = 'BODY_WATER_MASS'",
        "00000000-0000-0000-0000-000000000001");
    int before = rowCount();

    runSeedMigrationAgain();

    assertThat(rowCount()).isEqualTo(before);
    assertThat(storedTypeNames()).containsExactlyInAnyOrderElementsOf(expectedTypeNames());
  }

  @Test
  void seedMigrationRestoresAMissingRowAndKeepsExistingOnes() throws IOException {
    jdbc.update("delete from measurement_type where measurement_type = 'FAT_MASS'");
    jdbc.update("update measurement_type set max_value = 123 where measurement_type = 'BODY_IMPEDANCE'");

    runSeedMigrationAgain();

    assertThat(storedTypeNames()).containsExactlyInAnyOrderElementsOf(expectedTypeNames());
    assertThat(jdbc.queryForObject(
        "select max_value from measurement_type where measurement_type = 'BODY_IMPEDANCE'",
        Double.class)).isEqualTo(123.0);
  }

  private record MeasurementTypeRowCheck(
      String type, String unit, double min, double max, String observationId) {}
}
