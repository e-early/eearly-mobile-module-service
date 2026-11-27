package si.result.eearly.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import si.result.eearly.domain.measurement.Measurement;

/**
 * MapStruct only null-guards properties it also converts; {@code comment} is a plain String copy,
 * so without a forced null check the generated code would call the protobuf setter of an
 * {@code optional string} field with null, which throws.
 */
class MeasurementMapperTest {

  private final MeasurementMapper mapper = Mappers.getMapper(MeasurementMapper.class);

  private static Measurement measurement(String comment) {
    return new Measurement(
        UUID.randomUUID(),
        "openEHR-EHR-OBSERVATION.body_temperature.v2",
        UUID.randomUUID(),
        "Cel",
        37.3,
        UUID.randomUUID(),
        ZonedDateTime.parse("2026-01-01T10:00:00Z"),
        null,
        comment
    );
  }

  @Test
  void aNullCommentIsLeftUnsetRatherThanThrowing() {
    assertDoesNotThrow(() -> mapper.toGrpcMeasurement(measurement(null)));

    assertThat(mapper.toGrpcMeasurement(measurement(null)).hasComment()).isFalse();
  }

  @Test
  void aPresentCommentIsMappedThrough() {
    si.result.eearly.genproto.Measurement grpc =
        mapper.toGrpcMeasurement(measurement("test comment"));

    assertThat(grpc.hasComment()).isTrue();
    assertThat(grpc.getComment()).isEqualTo("test comment");
  }
}
