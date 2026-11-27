package si.result.eearly.service.onboarding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import si.result.eearly.domain.onboarding.OnboardingCode;
import si.result.eearly.repository.OnboardingCodeRepository;

/**
 * The onboarding_code table after migrating an empty database, and the redemption against a real
 * PostgreSQL (including two concurrent redemptions of one code).
 */
@DataJpaTest(properties = {
    "spring.flyway.enabled=true",
    "spring.flyway.schemas=eearly_mobile",
    "spring.flyway.locations=classpath:db/migration/common",
    "spring.datasource.hikari.schema=eearly_mobile"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OnboardingCodeMigrationIntegrationTest {

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @SpringBootConfiguration
  @EntityScan("si.result.eearly.domain")
  @EnableJpaRepositories(basePackageClasses = OnboardingCodeRepository.class)
  static class Config {
    @Bean
    OnboardingCodeService onboardingCodeService(OnboardingCodeRepository repository) {
      return new OnboardingCodeService(repository, Clock.systemUTC(), Duration.ofDays(30));
    }
  }

  @Autowired private JdbcTemplate jdbc;
  @Autowired private OnboardingCodeRepository repository;
  @Autowired private OnboardingCodeService service;

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void tableHasTheExpectedColumnsAndIndex() {
    List<Map<String, Object>> columns = jdbc.queryForList("""
        select column_name, data_type, character_maximum_length, is_nullable
        from information_schema.columns
        where table_schema = 'eearly_mobile' and table_name = 'onboarding_code'
        order by column_name
        """);

    assertThat(columns).extracting(c -> c.get("column_name")).containsExactly(
        "code_hash", "created_at", "expires_at", "id", "used_at", "user_id");
    Map<String, Map<String, Object>> byName = new java.util.HashMap<>();
    columns.forEach(c -> byName.put((String) c.get("column_name"), c));
    assertThat(byName.get("id")).containsEntry("data_type", "uuid").containsEntry("is_nullable", "NO");
    assertThat(byName.get("user_id")).containsEntry("data_type", "uuid")
        .containsEntry("is_nullable", "NO");
    assertThat(byName.get("code_hash")).containsEntry("data_type", "character varying")
        .containsEntry("character_maximum_length", 64).containsEntry("is_nullable", "NO");
    for (String name : List.of("expires_at", "created_at")) {
      assertThat(byName.get(name)).containsEntry("data_type", "timestamp with time zone")
          .containsEntry("is_nullable", "NO");
    }
    assertThat(byName.get("used_at")).containsEntry("data_type", "timestamp with time zone")
        .containsEntry("is_nullable", "YES");

    List<String> indexes = jdbc.queryForList("""
        select indexdef from pg_indexes
        where schemaname = 'eearly_mobile' and tablename = 'onboarding_code'
        """, String.class);
    assertThat(indexes).anyMatch(def -> def.contains("(user_id)"));
  }

  @Test
  void codeHashIsUnique() {
    Instant now = Instant.now();
    String hash = OnboardingCodeService.hash(OnboardingCodeService.generateCode());
    repository.saveAndFlush(
        OnboardingCode.create(UUID.randomUUID(), hash, now, now.plusSeconds(60)));

    assertThatThrownBy(() -> repository.saveAndFlush(
        OnboardingCode.create(UUID.randomUUID(), hash, now, now.plusSeconds(60))))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void validCodeRedeemsOnceAndOnlyTheHashIsStored() {
    UUID userId = UUID.randomUUID();
    String code = service.issue(userId);

    assertThat(service.redeem(code)).contains(userId);
    assertThat(service.redeem(code)).isEmpty();

    OnboardingCode stored = repository.findByCodeHash(OnboardingCodeService.hash(code)).orElseThrow();
    assertThat(stored.getUsedAt()).isNotNull();
    assertThat(jdbc.queryForObject("select count(*) from onboarding_code where code_hash = ?",
        Integer.class, code)).isZero();
  }

  @Test
  void expiredAndUnknownCodesAndRawUserIdsAreRejected() {
    UUID userId = UUID.randomUUID();
    String expired = OnboardingCodeService.generateCode();
    Instant now = Instant.now();
    repository.saveAndFlush(OnboardingCode.create(userId, OnboardingCodeService.hash(expired),
        now.minus(Duration.ofDays(31)), now.minusSeconds(1)));

    assertThat(service.redeem(expired)).isEmpty();
    assertThat(service.redeem(OnboardingCodeService.generateCode())).isEmpty();
    assertThat(service.redeem(userId.toString())).isEmpty();
  }

  @Test
  void aNewLinkInvalidatesTheUnusedCodesOfTheUserOnly() {
    UUID userId = UUID.randomUUID();
    UUID otherUser = UUID.randomUUID();
    String first = service.issue(userId);
    String second = service.issue(userId);
    String othersCode = service.issue(otherUser);

    String replacement = service.replace(userId);

    assertThat(service.redeem(first)).isEmpty();
    assertThat(service.redeem(second)).isEmpty();
    assertThat(service.redeem(othersCode)).contains(otherUser);
    assertThat(service.redeem(replacement)).contains(userId);
  }

  @Test
  void concurrentRedemptionsOfOneCodeSucceedExactlyOnce() throws Exception {
    UUID userId = UUID.randomUUID();
    String code = service.issue(userId);
    int threads = 8;
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Boolean>> results = new ArrayList<>();
      for (int i = 0; i < threads; i++) {
        Callable<Boolean> task = () -> {
          start.await();
          return service.redeem(code).isPresent();
        };
        results.add(executor.submit(task));
      }
      start.countDown();

      int successes = 0;
      for (Future<Boolean> result : results) {
        if (result.get()) {
          successes++;
        }
      }
      assertThat(successes).isEqualTo(1);
    } finally {
      executor.shutdownNow();
    }
  }
}
