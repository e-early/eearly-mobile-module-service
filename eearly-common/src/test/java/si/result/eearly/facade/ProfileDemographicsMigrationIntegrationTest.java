package si.result.eearly.facade;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import si.result.eearly.domain.enums.Sex;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.repository.ProfileRepository;

/** The profile demographics columns exist after migrating an empty database and map to Profile. */
@DataJpaTest(properties = {
    "spring.flyway.enabled=true",
    "spring.flyway.schemas=eearly_mobile",
    "spring.flyway.locations=classpath:db/migration/common",
    "spring.datasource.hikari.schema=eearly_mobile"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ProfileDemographicsMigrationIntegrationTest {

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @SpringBootConfiguration
  @EntityScan("si.result.eearly.domain")
  @EnableJpaRepositories(basePackageClasses = ProfileRepository.class)
  static class Config {}

  @Autowired private JdbcTemplate jdbc;
  @Autowired private ProfileRepository profileRepository;
  @Autowired private TestEntityManager entityManager;

  @Test
  void demographicsColumnsAreNullableDateAndShortText() {
    List<Map<String, Object>> columns = jdbc.queryForList("""
        select column_name, data_type, character_maximum_length, is_nullable
        from information_schema.columns
        where table_schema = 'eearly_mobile' and table_name = 'profile'
          and column_name in ('date_of_birth', 'sex')
        order by column_name
        """);

    assertThat(columns).hasSize(2);
    assertThat(columns.get(0)).containsEntry("column_name", "date_of_birth")
        .containsEntry("data_type", "date").containsEntry("is_nullable", "YES");
    assertThat(columns.get(1)).containsEntry("column_name", "sex")
        .containsEntry("data_type", "character varying")
        .containsEntry("character_maximum_length", 16)
        .containsEntry("is_nullable", "YES");
  }

  @Test
  void languageColumnIsRequiredAndDefaultsToSlovenian() {
    List<Map<String, Object>> columns = jdbc.queryForList("""
        select data_type, character_maximum_length, is_nullable, column_default
        from information_schema.columns
        where table_schema = 'eearly_mobile' and table_name = 'profile'
          and column_name = 'language'
        """);

    assertThat(columns).hasSize(1);
    assertThat(columns.get(0)).containsEntry("data_type", "character varying")
        .containsEntry("character_maximum_length", 3)
        .containsEntry("is_nullable", "NO");
    assertThat(String.valueOf(columns.get(0).get("column_default"))).contains("'sl'");

    UUID userId = UUID.randomUUID();
    profileRepository.saveAndFlush(Profile.createProfileFromId(userId.toString()));
    entityManager.clear();
    assertThat(profileRepository.findById(userId).orElseThrow().getLanguage()).isEqualTo("sl");
  }

  @Test
  void profileDemographicsRoundTrip() {
    UUID userId = UUID.randomUUID();
    Profile profile = Profile.createProfileFromId(userId.toString());
    profile.setDateOfBirth(LocalDate.parse("1980-05-17"));
    profile.setSex(Sex.FEMALE);
    profileRepository.saveAndFlush(profile);
    entityManager.clear();

    Profile loaded = profileRepository.findById(userId).orElseThrow();
    assertThat(loaded.getDateOfBirth()).isEqualTo(LocalDate.parse("1980-05-17"));
    assertThat(loaded.getSex()).isEqualTo(Sex.FEMALE);
    assertThat(jdbc.queryForObject(
        "select sex from profile where user_id = ?", String.class, userId)).isEqualTo("FEMALE");
  }
}
