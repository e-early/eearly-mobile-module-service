package si.result.eearly.ehr;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import si.result.eearly.domain.measurement.GetEhrMeasurementsRequest;
import si.result.eearly.domain.measurement.GetMeasurementsCommand;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementPage;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.service.ehr.EhrKeycloakService;

@Service
@Slf4j
public class EhrbaseClient {

  private WebClient ehrWebClient;
  private EhrKeycloakService ehrKeycloakService;


  @Autowired
  private void setEhrWebClient(@Qualifier("ehrbaseWebClient") WebClient webClient,
                               EhrKeycloakService ehrKeycloakService) {
    this.ehrWebClient = webClient;
    this.ehrKeycloakService = ehrKeycloakService;
  }

  public void createEhr(UUID id) {
    var token = ehrKeycloakService.getAccessTokenForClient();
    try {
      ehrWebClient.put()
          .uri("ehr/" + id)
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer " + token)
          .retrieve()
          .bodyToMono(Void.class)
          .block();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create ehr", e);
    }
  }

  public boolean doesEhrExist(UUID id) {
    var token = ehrKeycloakService.getAccessTokenForClient();
    try {
      ehrWebClient.get()
          .uri("ehr/" + id)
          .header("Authorization", "Bearer " + token)
          .retrieve()
          .bodyToMono(String.class)
          .block();
      return true;
    } catch (WebClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return false;
      }
      throw new RuntimeException(
          String.format("Invalid status when fetching EHR from Ehrbase: %s", e.getStatusCode()));
    } catch (Exception e) {
      throw new RuntimeException("Failed to get ehr", e);
    }
  }

  public List<Measurement> findAll(GetMeasurementsCommand command) {
    var token = ehrKeycloakService.getAccessTokenForClient();
    if (command.ehrId() == null) {
      throw new RuntimeException("Missing ehr id");
    }

    final int fetch = 5000;
    final int parallelism = 5;

    final String dataQuery = buildQuery(command);
    final String countQuery = buildCountQuery(command);

    JsonNode countResponse = executeAql(token, countQuery, 1, 0).block();

    if (countResponse == null
        || !countResponse.has("rows")
        || countResponse.get("rows").isEmpty()) {
      return Collections.emptyList();
    }

    int totalCount = countResponse.get("rows").get(0).get(0).asInt();
    if (totalCount == 0) {
      return Collections.emptyList();
    }

    int totalPages = (int) Math.ceil((double) totalCount / fetch);

    return Flux.range(0, totalPages)
        .flatMap(pageIndex -> {

          int offset = pageIndex * fetch;

          return executeAql(token, dataQuery, fetch, offset)
              .flatMapMany(resp -> {
                if (resp == null
                    || !resp.has("rows")
                    || resp.get("rows").isEmpty()) {
                  return Flux.empty();
                }
                return Flux.fromIterable(resp.get("rows"));
              })
              .map(row -> convertRowToMeasurement(row, command));

        }, parallelism)
        .collectList()
        .block();
  }

  public MeasurementPage findPage(GetMeasurementsCommand command, int offset, int limit) {
    var token = ehrKeycloakService.getAccessTokenForClient();
    if (command.ehrId() == null) {
      throw new RuntimeException("Missing ehr id");
    }

    if (limit <= 0) {
      return new MeasurementPage(Collections.emptyList(), 0);
    }

    final String dataQuery = buildQuery(command) + "\nORDER BY obs/data/events/time/value ASC";
    final String countQuery = buildCountQuery(command);

    JsonNode countResponse = executeAql(token, countQuery, 1, 0).block();

    if (countResponse == null
        || !countResponse.has("rows")
        || countResponse.get("rows").isEmpty()) {
      return new MeasurementPage(Collections.emptyList(), 0);
    }

    int totalCount = countResponse.get("rows").get(0).get(0).asInt();
    if (totalCount == 0 || offset >= totalCount) {
      return new MeasurementPage(Collections.emptyList(), totalCount);
    }

    JsonNode response = executeAql(token, dataQuery, limit, offset).block();
    if (response == null
        || !response.has("rows")
        || response.get("rows").isEmpty()) {
      return new MeasurementPage(Collections.emptyList(), totalCount);
    }

    List<Measurement> measurements = Flux.fromIterable(response.get("rows"))
        .map(row -> convertRowToMeasurement(row, command))
        .collectList()
        .block();

    return new MeasurementPage(measurements == null ? Collections.emptyList() : measurements,
        totalCount);
  }

  private String buildQuery(GetMeasurementsCommand command) {
    return """
        SELECT
            obs/archetype_node_id AS measurement_type,
            obs/data/events/data/items/name/value AS measurement_name,
            obs/data/events/data/items/value/units AS units,
            obs/data/events/data/items/value/magnitude AS magnitude,
            c/uid/value AS batch_id,
            obs/data/events/time/value AS measurement_time,
            obs/protocol/items/items[at0021]/value/id AS device_id
        FROM EHR e
        CONTAINS COMPOSITION c
        CONTAINS OBSERVATION obs
        WHERE %s
        """.formatted(buildWhereClause(command));
  }

  private String buildCountQuery(GetMeasurementsCommand command) {

    return """
        SELECT COUNT(obs/data/events/data/items/value/magnitude) AS total_count
        FROM EHR e
        CONTAINS COMPOSITION c
        CONTAINS OBSERVATION obs
        WHERE %s
        """.formatted(buildWhereClause(command));
  }

  private String buildWhereClause(GetMeasurementsCommand command) {

    StringBuilder where = new StringBuilder();
    where.append("e/ehr_id/value = '")
        .append(command.ehrId())
        .append("'");

    if (command.startDateTime() != null && !command.startDateTime().isEmpty()) {
      where.append(" AND obs/data/events/time/value >= '")
          .append(command.startDateTime())
          .append("'");
    }

    if (command.endDateTime() != null && !command.endDateTime().isEmpty()) {
      where.append(" AND obs/data/events/time/value <= '")
          .append(command.endDateTime())
          .append("'");
    }

    if (command.observationIds() != null && !command.observationIds().isEmpty()) {
      String matches = command.observationIds().stream()
          .map(id -> "'" + id + "'")
          .collect(Collectors.joining(", ", "{", "}"));

      where.append(" AND obs/archetype_node_id MATCHES ")
          .append(matches);
    }

    return where.toString();
  }

  private Mono<JsonNode> executeAql(String token, String query, int fetch, int offset) {
    return ehrWebClient.post()
        .uri("/query/aql")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + token)
        .bodyValue(new GetEhrMeasurementsRequest(query, fetch, offset))
        .retrieve()
        .bodyToMono(JsonNode.class);
  }

  private Measurement convertRowToMeasurement(JsonNode row, GetMeasurementsCommand command) {
    UUID batchId = null;
    try {
      batchId = UUID.fromString(row.get(4).asText().split("::")[0]);
    } catch (Exception e) {
      log.debug("could not parse batch id: {}", row.get(4).asText());
    }

    String archetypeId = row.get(0).asText();

    UUID deviceId = null;
    try {
      if (!row.get(6).isNull()) {
        deviceId = UUID.fromString(row.get(6).asText());
      }
    } catch (Exception e) {
      log.debug("could not parse device id: {}", row.get(6).asText());
    }

    UUID measurementTypeId = null;
    try {
      MeasurementType mt = EhrbaseMeasurementName.getMeasurementType(
          archetypeId,
          row.get(1).asText(),
          command.measurementTypes());
      measurementTypeId = mt.getId();
    } catch (Exception e) {
      log.debug("Could not determine measurement typeId from archetype {} and measurement name {}", archetypeId, row.get(1).asText());
    }

    return new Measurement(
        command.ehrId(),
        archetypeId,
        measurementTypeId,
        row.get(2).asText(),
        row.get(3).asDouble(),
        batchId,
        ZonedDateTime.parse(row.get(5).asText()),
        deviceId
    );
  }

  public void createContribution(UUID ehrId, String contribution) {
    var token = ehrKeycloakService.getAccessTokenForClient();
    try {
      ehrWebClient.post()
          .uri("/ehr/" + ehrId + "/composition")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer " + token)
          .bodyValue(contribution)
          .retrieve()
          .bodyToMono(String.class)
          .block();
    } catch (Exception e) {
      int x = 5;
      throw new RuntimeException(e);
    }

  }
}
