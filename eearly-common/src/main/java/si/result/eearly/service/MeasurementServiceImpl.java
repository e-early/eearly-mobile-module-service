package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.measurement.CreateMeasurementCommand;
import si.result.eearly.domain.measurement.GetMeasurementsCommand;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementKey;
import si.result.eearly.domain.measurement.MeasurementPage;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.ehr.EhrMedicalDevice;
import si.result.eearly.mapper.ehr.EhrMapper;
import si.result.eearly.repository.DeviceRepository;
import si.result.eearly.repository.MeasurementRepository;
import si.result.eearly.repository.MeasurementTypeRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeasurementServiceImpl implements MeasurementService {

	private final MeasurementRepository measurementRepository;
  private final MeasurementTypeRepository measurementTypeRepository;
  private final DeviceRepository deviceRepository;
  private final Map<String, EhrMapper> ehrMappers;
  private final ApiDeviceServiceWrapper apiDeviceServiceWrapper;

  /**
   * Writes one EHR composition per (batch, measurement type).
   *
   * <p>A single batch can carry more than one measurement type (for example a device reporting
   * heart rate and oxygen saturation from one reading). Each type must reach the mapper that
   * understands it, so grouping by batch id alone is not enough: the whole batch would be handed
   * to the mapper of whichever measurement came first.
   *
   * <p>Grouping uses the resolved measurement <em>type</em> and not the measurement type id:
   * systolic and diastolic blood pressure are separate type ids that share the
   * {@code BLOOD_PRESSURE} type, and its mapper needs both to build a single composition.
   *
   * <p>Measurements whose type is unknown, or for which no mapper is registered, are skipped and
   * logged at ERROR level (batch id, type and count only) without failing the rest of the batch.
   */
  @Override
  public void addBatchMeasurements(List<CreateMeasurementCommand> commands) {
    List<MeasurementType> measurementTypeList = measurementTypeRepository.findAll();

    Map<BatchTypeKey, List<CreateMeasurementCommand>> groupedBatchMeasurementCommands =
        new LinkedHashMap<>();
    Map<BatchTypeKey, Integer> unknownTypeCounts = new LinkedHashMap<>();

    for (CreateMeasurementCommand command : commands) {
      Optional<MeasurementType> measurementType =
          findMeasurementType(measurementTypeList, command.measurementTypeId());
      if (measurementType.isEmpty()) {
        unknownTypeCounts.merge(
            new BatchTypeKey(command.measurementBatchId(), "type id " + command.measurementTypeId()),
            1, Integer::sum);
        continue;
      }

      BatchTypeKey key = new BatchTypeKey(command.measurementBatchId(),
          measurementType.get().getMeasurementType().toString());
      groupedBatchMeasurementCommands.computeIfAbsent(key, ignored -> new ArrayList<>())
          .add(command);
    }

    unknownTypeCounts.forEach((key, count) ->
        log.error("Could not determine measurement type for batch {} ({}), skipping {} measurement(s)",
            key.batchId(), key.measurementType(), count));

    groupedBatchMeasurementCommands.forEach((key, measurements) -> {
      EhrMapper ehrMapper = ehrMappers.get(key.measurementType());
      if (ehrMapper == null) {
        log.error("No EHR mapper registered for measurement type {} in batch {}, skipping {} measurement(s)",
            key.measurementType(), key.batchId(), measurements.size());
        return;
      }

      String deviceId = measurements.getFirst().deviceId();
      EhrMedicalDevice medicalDevice = null;
      if (deviceId != null && !deviceId.isEmpty()) {
        var device = deviceRepository.findById(UUID.fromString(deviceId));
        if (device.isPresent()) {
          medicalDevice = new EhrMedicalDevice(deviceId, device.get().getName());
        }
      }

      String marshalledComposition = ehrMapper.marshallMeasurements(measurements, medicalDevice, measurementTypeList);
      measurementRepository.create(marshalledComposition, UUID.fromString(commands.getFirst().userId()));
    });
  }

  private static Optional<MeasurementType> findMeasurementType(List<MeasurementType> measurementTypes,
      String measurementTypeId) {
    return measurementTypes.stream()
        .filter(type -> type.getId().toString().equals(measurementTypeId))
        .findFirst();
  }

  /** Identifies one composition: a batch and the measurement type written in it. */
  private record BatchTypeKey(String batchId, String measurementType) {
  }

	public List<Measurement> getMeasurementList(GetMeasurementsCommand command) {
    List<Measurement> ehrMeasurements = measurementRepository.findAll(command);
    List<Measurement> apiMeasurements = apiDeviceServiceWrapper.getMeasurements(command);

    List<Measurement> merged = mergeDistinctApiMeasurements(ehrMeasurements, apiMeasurements);

    return merged.stream()
        .sorted(Comparator.comparing(Measurement::measurementTime))
        .toList();
	}

  public MeasurementPage getMeasurementPage(GetMeasurementsCommand command, int offset, int limit) {
    int ehrLimit = (int) Math.min((long) offset + limit, Integer.MAX_VALUE);
    MeasurementPage ehrMeasurementPage = measurementRepository.findPage(command, 0, ehrLimit);
    List<Measurement> apiMeasurements = apiDeviceServiceWrapper.getMeasurements(command);

    List<Measurement> merged = mergeDistinctApiMeasurements(ehrMeasurementPage.measurements(), apiMeasurements);

    List<Measurement> page = merged.stream()
        .sorted(Comparator.comparing(Measurement::measurementTime))
        .skip(offset)
        .limit(limit)
        .toList();

    return new MeasurementPage(page, ehrMeasurementPage.totalElements());
  }

  /**
   * Appends API-sourced measurements that don't already have a matching EHRbase measurement.
   *
   * <p>EHRbase's own rows are never deduplicated against each other here: two distinct readings
   * can legitimately share a timestamp and value (e.g. two sensors reporting in the same second),
   * and folding all EHRbase rows through one key map silently dropped one of every such pair.
   */
  private static List<Measurement> mergeDistinctApiMeasurements(List<Measurement> ehrMeasurements,
      List<Measurement> apiMeasurements) {
    Set<MeasurementKey> ehrKeys = new HashSet<>();
    for (Measurement measurement : ehrMeasurements) {
      ehrKeys.add(measurement.toMeasurementKey());
    }

    List<Measurement> merged = new ArrayList<>(ehrMeasurements);
    for (Measurement measurement : apiMeasurements) {
      if (ehrKeys.add(measurement.toMeasurementKey())) {
        merged.add(measurement);
      }
    }
    return merged;
  }
}
