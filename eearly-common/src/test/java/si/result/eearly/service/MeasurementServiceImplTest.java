package si.result.eearly.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import si.result.eearly.domain.measurement.GetMeasurementsCommand;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementPage;
import si.result.eearly.mapper.ehr.EhrMapper;
import si.result.eearly.repository.DeviceRepository;
import si.result.eearly.repository.MeasurementRepository;
import si.result.eearly.repository.MeasurementTypeRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
}
