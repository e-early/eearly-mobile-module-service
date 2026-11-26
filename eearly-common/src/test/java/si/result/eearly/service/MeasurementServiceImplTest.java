package si.result.eearly.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import si.result.eearly.domain.enums.MeasurementSubTypeEnum;
import si.result.eearly.domain.enums.MeasurementTypeEnum;
import si.result.eearly.domain.measurement.CreateMeasurementCommand;
import si.result.eearly.domain.measurement.GetMeasurementsCommand;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementPage;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.mapper.ehr.EhrMapper;
import si.result.eearly.repository.DeviceRepository;
import si.result.eearly.repository.MeasurementRepository;
import si.result.eearly.repository.MeasurementTypeRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Reproduces the reported bug: two overlapping series written to one EHR (1100 measurements
 * total, some sharing a timestamp) came back from EHRbase's own COUNT as 1100, but this
 * service's paged endpoint returned only 1000 - because {@code getMeasurementPage} folded every
 * EHRbase row through a {@code Map<MeasurementKey, Measurement>} keyed on
 * (type, deviceId, userId, timestamp, value), and EHRbase-sourced rows always carry a null
 * deviceId. Two distinct readings at the same second with the same value collided in that map
 * and one was silently dropped, even though both came straight from EHRbase.
 */
@ExtendWith(MockitoExtension.class)
class MeasurementServiceImplTest {

  private static final UUID HEART_RATE_ID = UUID.randomUUID();
  private static final UUID OXYGEN_SATURATION_ID = UUID.randomUUID();
  private static final UUID SYSTOLIC_ID = UUID.randomUUID();
  private static final UUID DIASTOLIC_ID = UUID.randomUUID();
  private static final UUID BODY_HEIGHT_ID = UUID.randomUUID();
  private static final String USER_ID = UUID.randomUUID().toString();

  @Mock
  private MeasurementRepository measurementRepository;
  @Mock
  private MeasurementTypeRepository measurementTypeRepository;
  @Mock
  private DeviceRepository deviceRepository;
  @Mock
  private ApiDeviceServiceWrapper apiDeviceServiceWrapper;

  @Test
  @DisplayName("getMeasurementPage returns every EHRbase row, including ones sharing a timestamp and value")
  void keepsEveryEhrMeasurementEvenWhenTimestampsAndValuesCollide() {
    Map<String, EhrMapper> mappers = Map.of();
    MeasurementServiceImpl service = new MeasurementServiceImpl(measurementRepository,
        measurementTypeRepository, deviceRepository, mappers, apiDeviceServiceWrapper);

    UUID ehrId = UUID.randomUUID();
    OffsetDateTime start = OffsetDateTime.parse("2026-08-19T09:00:00Z");

    List<Measurement> ehrMeasurements = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      ehrMeasurements.add(heartRateAt(ehrId, start.plusSeconds(i), 60 + (i % 40)));
    }
    // 100 more real readings that collide with the first 100 above on (type, timestamp, value).
    for (int i = 0; i < 100; i++) {
      ehrMeasurements.add(heartRateAt(ehrId, start.plusSeconds(i), 60 + (i % 40)));
    }
    assertThat(ehrMeasurements).hasSize(1100);

    when(apiDeviceServiceWrapper.getMeasurements(any())).thenReturn(List.of());
    when(measurementRepository.findPage(any(), eq(0), eq(1100)))
        .thenReturn(new MeasurementPage(ehrMeasurements, 1100));

    MeasurementPage page = service.getMeasurementPage(
        new GetMeasurementsCommand(ehrId, List.of(), null, null, List.of()), 0, 1100);

    assertThat(page.totalElements()).isEqualTo(1100);
    assertThat(page.measurements()).as("no EHRbase row should be dropped just for sharing a "
        + "timestamp and value with another one").hasSize(1100);
  }

  private static Measurement heartRateAt(UUID ehrId, OffsetDateTime measuredAt, double bpm) {
    return new Measurement(ehrId, "openEHR-EHR-OBSERVATION.pulse.v2", HEART_RATE_ID, "/min", bpm,
        UUID.randomUUID(), measuredAt.atZoneSameInstant(ZoneOffset.UTC), null);
  }

  // --- addBatchMeasurements: one composition per (batch, measurement type) ---

  /** Mapper mocks keyed by measurement type name; body height deliberately has none. */
  private final Map<String, EhrMapper> mappers = new HashMap<>();

  private MeasurementServiceImpl batchService() {
    for (String type : List.of("HEART_RATE", "OXYGEN_SATURATION", "BLOOD_PRESSURE")) {
      EhrMapper mapper = mock(EhrMapper.class);
      lenient().when(mapper.marshallMeasurements(any(), any(), any()))
          .thenReturn("{\"composition\":\"" + type + "\"}");
      mappers.put(type, mapper);
    }
    lenient().when(measurementTypeRepository.findAll()).thenReturn(List.of(
        measurementType(HEART_RATE_ID, MeasurementTypeEnum.HEART_RATE,
            MeasurementSubTypeEnum.UNKNOWN_MEASUREMENT_SUB_TYPE),
        measurementType(OXYGEN_SATURATION_ID, MeasurementTypeEnum.OXYGEN_SATURATION,
            MeasurementSubTypeEnum.UNKNOWN_MEASUREMENT_SUB_TYPE),
        measurementType(SYSTOLIC_ID, MeasurementTypeEnum.BLOOD_PRESSURE,
            MeasurementSubTypeEnum.SYSTOLIC),
        measurementType(DIASTOLIC_ID, MeasurementTypeEnum.BLOOD_PRESSURE,
            MeasurementSubTypeEnum.DIASTOLIC),
        measurementType(BODY_HEIGHT_ID, MeasurementTypeEnum.BODY_HEIGHT,
            MeasurementSubTypeEnum.UNKNOWN_MEASUREMENT_SUB_TYPE)));
    return new MeasurementServiceImpl(measurementRepository, measurementTypeRepository,
        deviceRepository, mappers, apiDeviceServiceWrapper);
  }

  private static MeasurementType measurementType(UUID id, MeasurementTypeEnum type,
      MeasurementSubTypeEnum subType) {
    MeasurementType measurementType = new MeasurementType();
    measurementType.setId(id);
    measurementType.setMeasurementType(type);
    measurementType.setMeasurementSubType(subType);
    return measurementType;
  }

  private static CreateMeasurementCommand command(UUID typeId, double value, String batchId) {
    return new CreateMeasurementCommand(OffsetDateTime.parse("2026-01-15T09:00:00Z"),
        typeId.toString(), value, "", USER_ID, 0, batchId, "SINGLE");
  }

  @SuppressWarnings("unchecked")
  private List<CreateMeasurementCommand> handedTo(String type) {
    org.mockito.ArgumentCaptor<List<CreateMeasurementCommand>> captor =
        org.mockito.ArgumentCaptor.forClass(List.class);
    verify(mappers.get(type)).marshallMeasurements(captor.capture(), any(), any());
    return captor.getValue();
  }

  @Test
  @DisplayName("addBatchMeasurements sends each measurement type of a batch to its own mapper")
  void splitsMixedTypeBatchByMeasurementType() {
    MeasurementServiceImpl service = batchService();
    String batchId = UUID.randomUUID().toString();

    service.addBatchMeasurements(List.of(
        command(HEART_RATE_ID, 80.0, batchId),
        command(OXYGEN_SATURATION_ID, 97.0, batchId)));

    assertThat(handedTo("HEART_RATE")).extracting(CreateMeasurementCommand::value)
        .containsExactly(80.0);
    assertThat(handedTo("OXYGEN_SATURATION")).extracting(CreateMeasurementCommand::value)
        .containsExactly(97.0);
    verify(measurementRepository, times(2)).create(anyString(), any());
  }

  @Test
  @DisplayName("addBatchMeasurements keeps systolic and diastolic together in one composition")
  void keepsSubTypesOfOneMeasurementTypeTogether() {
    MeasurementServiceImpl service = batchService();
    String batchId = UUID.randomUUID().toString();

    service.addBatchMeasurements(List.of(
        command(SYSTOLIC_ID, 120.0, batchId),
        command(DIASTOLIC_ID, 80.0, batchId)));

    assertThat(handedTo("BLOOD_PRESSURE")).extracting(CreateMeasurementCommand::value)
        .containsExactlyInAnyOrder(120.0, 80.0);
    verify(measurementRepository, times(1)).create(anyString(), any());
  }

  @Test
  @DisplayName("addBatchMeasurements keeps separate batches apart for the same measurement type")
  void doesNotMergeSeparateBatches() {
    MeasurementServiceImpl service = batchService();

    service.addBatchMeasurements(List.of(
        command(HEART_RATE_ID, 70.0, UUID.randomUUID().toString()),
        command(HEART_RATE_ID, 75.0, UUID.randomUUID().toString())));

    verify(measurementRepository, times(2)).create(anyString(), any());
  }

  @Test
  @DisplayName("addBatchMeasurements skips an unknown measurement type id without failing the batch")
  void skipsUnknownMeasurementTypeId() {
    MeasurementServiceImpl service = batchService();
    String batchId = UUID.randomUUID().toString();

    service.addBatchMeasurements(List.of(
        command(UUID.randomUUID(), 5.0, batchId),
        command(HEART_RATE_ID, 80.0, batchId)));

    assertThat(handedTo("HEART_RATE")).extracting(CreateMeasurementCommand::value)
        .containsExactly(80.0);
    verify(measurementRepository, times(1)).create(anyString(), any());
  }

  @Test
  @DisplayName("addBatchMeasurements skips a measurement type without a mapper and still writes the rest")
  void skipsMeasurementTypeWithoutMapper() {
    MeasurementServiceImpl service = batchService();
    String batchId = UUID.randomUUID().toString();

    service.addBatchMeasurements(List.of(
        command(BODY_HEIGHT_ID, 170.0, batchId),
        command(HEART_RATE_ID, 80.0, batchId)));

    assertThat(handedTo("HEART_RATE")).extracting(CreateMeasurementCommand::value)
        .containsExactly(80.0);
    verify(mappers.get("OXYGEN_SATURATION"), never()).marshallMeasurements(any(), any(), any());
    verify(measurementRepository, times(1)).create(anyString(), any());
  }
}
