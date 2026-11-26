package si.result.eearly.ehr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import si.result.eearly.domain.enums.MeasurementTypeEnum;
import si.result.eearly.domain.measurement.GetMeasurementsCommand;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementPage;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.service.ehr.EhrKeycloakService;

/**
 * A comment is stored as its own sibling item next to a reading's magnitude (same event,
 * different at-code), not as an attribute of the same item. Without special handling a commented
 * reading comes back as two rows: the real one and a magnitude-less "Comment" row that would be
 * converted into a bogus 0.0 reading. These tests pin that the per-reading and count queries
 * exclude the comment item, that a separate query selects only it, and that the comment is
 * attached by batch id rather than by row.
 */
class EhrbaseClientTest {

  private static final String BODY_TEMPERATURE_ID = "openEHR-EHR-OBSERVATION.body_temperature.v2";

  private final EhrbaseClient client = new EhrbaseClient();

  private static GetMeasurementsCommand command(MeasurementType... types) {
    return new GetMeasurementsCommand(UUID.randomUUID(), List.of(), null, null, List.of(types));
  }

  private static MeasurementType measurementType(UUID id, MeasurementTypeEnum type) {
    MeasurementType measurementType = new MeasurementType();
    measurementType.setId(id);
    measurementType.setMeasurementType(type);
    return measurementType;
  }

  @Test
  void buildQueryExcludesTheCommentItem() {
    String query = client.buildQuery(command());

    assertThat(query).contains("obs/data/events/data/items/name/value != 'Comment'");
  }

  @Test
  void buildCountQueryExcludesTheCommentItem() {
    String query = client.buildCountQuery(command());

    assertThat(query).contains("obs/data/events/data/items/name/value != 'Comment'");
  }

  @Test
  void buildCommentQuerySelectsOnlyTheCommentItem() {
    String query = client.buildCommentQuery(command());

    assertThat(query)
        .contains("obs/data/events/data/items/name/value = 'Comment'")
        .contains("obs/data/events/data/items/value/value AS comment_value")
        .contains("c/uid/value AS batch_id");
  }

  @Test
  void isMeasurementRowRejectsRowsWithoutAMagnitude() {
    UUID batchId = UUID.randomUUID();
    ArrayNode commentRow = row(BODY_TEMPERATURE_ID, "Comment", null, null,
        batchId + "::host::1", "2026-01-01T10:00:00Z");
    ArrayNode readingRow = row(BODY_TEMPERATURE_ID, "Temperature", "Cel", 37.3,
        batchId + "::host::1", "2026-01-01T10:00:00Z");

    assertThat(EhrbaseClient.isMeasurementRow(commentRow)).isFalse();
    assertThat(EhrbaseClient.isMeasurementRow(readingRow)).isTrue();
  }

  @Test
  void convertRowToMeasurementAttachesTheCommentForItsBatch() {
    UUID batchId = UUID.randomUUID();
    GetMeasurementsCommand command = command(
        measurementType(UUID.randomUUID(), MeasurementTypeEnum.BODY_TEMPERATURE));
    ArrayNode row = row(BODY_TEMPERATURE_ID, "Temperature", "Cel", 37.3,
        batchId + "::host::1", "2026-01-01T10:00:00Z");

    Measurement measurement = client.convertRowToMeasurement(row, command,
        Map.of(batchId, "test comment"));

    assertThat(measurement.comment()).isEqualTo("test comment");
    assertThat(measurement.magnitude()).isEqualTo(37.3);
  }

  @Test
  void convertRowToMeasurementLeavesCommentNullWhenBatchHasNone() {
    UUID batchId = UUID.randomUUID();
    GetMeasurementsCommand command = command(
        measurementType(UUID.randomUUID(), MeasurementTypeEnum.BODY_TEMPERATURE));
    ArrayNode row = row(BODY_TEMPERATURE_ID, "Temperature", "Cel", 37.3,
        batchId + "::host::1", "2026-01-01T10:00:00Z");

    Measurement measurement = client.convertRowToMeasurement(row, command, Map.of());

    assertThat(measurement.comment()).isNull();
  }

  @Test
  void findPageDropsCommentRowsAndAttachesTheCommentToTheReading() throws Exception {
    UUID batchId = UUID.randomUUID();
    UUID typeId = UUID.randomUUID();
    GetMeasurementsCommand command = command(
        measurementType(typeId, MeasurementTypeEnum.BODY_TEMPERATURE));

    // Responses in call order: count query, data query, comment query.
    Deque<String> responses = new ArrayDeque<>(List.of(
        "{\"rows\": [[1]]}",
        "{\"rows\": ["
            + "[\"" + BODY_TEMPERATURE_ID + "\", \"Temperature\", \"Cel\", 37.3, \""
            + batchId + "::host::1\", \"2026-01-01T10:00:00Z\", null],"
            + "[\"" + BODY_TEMPERATURE_ID + "\", \"Comment\", null, null, \""
            + batchId + "::host::1\", \"2026-01-01T10:00:00Z\", null]"
            + "]}",
        "{\"rows\": [[\"" + batchId + "::host::1\", \"test comment\"]]}"));
    EhrbaseClient fakeBackedClient = clientReturning(responses);

    MeasurementPage page = fakeBackedClient.findPage(command, 0, 10);

    assertThat(page.measurements()).hasSize(1);
    Measurement measurement = page.measurements().get(0);
    assertThat(measurement.magnitude()).isEqualTo(37.3);
    assertThat(measurement.measurementTypeId()).isEqualTo(typeId);
    assertThat(measurement.comment()).isEqualTo("test comment");
  }

  private static EhrbaseClient clientReturning(Deque<String> responses) throws Exception {
    WebClient webClient = WebClient.builder()
        .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(responses.removeFirst())
            .build()))
        .build();
    EhrKeycloakService keycloak = mock(EhrKeycloakService.class);
    when(keycloak.getAccessTokenForClient()).thenReturn("test-token");

    EhrbaseClient target = new EhrbaseClient();
    Method setter = EhrbaseClient.class.getDeclaredMethod("setEhrWebClient", WebClient.class,
        EhrKeycloakService.class);
    setter.setAccessible(true);
    setter.invoke(target, webClient, keycloak);
    return target;
  }

  /** Matches {@link EhrbaseClient#buildQuery}'s column order: type, name, units, magnitude, batch id, time, device. */
  private static ArrayNode row(String archetypeId, String name, String units, Double magnitude,
      String batchId, String measurementTime) {
    JsonNodeFactory factory = JsonNodeFactory.instance;
    ArrayNode row = factory.arrayNode();
    row.add(archetypeId);
    row.add(name);
    row.add(units);
    if (magnitude == null) {
      row.addNull();
    } else {
      row.add(magnitude);
    }
    row.add(batchId);
    row.add(measurementTime);
    row.addNull();
    return row;
  }
}
