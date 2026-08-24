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

	@Override
	public void addBatchMeasurements(List<CreateMeasurementCommand> commands) {
    Map<String, List<CreateMeasurementCommand>> groupedBatchMeasurementCommands = new HashMap<>();

    for (CreateMeasurementCommand command : commands) {
      String batchId = command.measurementBatchId();
      var currentMeasurementsForBatch = groupedBatchMeasurementCommands.get(batchId);
      if (currentMeasurementsForBatch == null) {
        groupedBatchMeasurementCommands.put(batchId, new ArrayList<>(List.of(command)));
      } else {
        groupedBatchMeasurementCommands.get(batchId).add(command);
      }
    }

    List<MeasurementType> measurementTypeList = measurementTypeRepository.findAll();

    groupedBatchMeasurementCommands.forEach((ignored, measurements) -> {
      String measurementTypeId = measurements.getFirst().measurementTypeId();
      String deviceId = measurements.getFirst().deviceId();
      EhrMedicalDevice medicalDevice = null;
      if(!deviceId.isEmpty()) {
        var device = deviceRepository.findById(UUID.fromString(deviceId));
        if(device.isPresent()) {
          medicalDevice = new EhrMedicalDevice(deviceId, device.get().getName());
        }
      }

      Optional<MeasurementType> measurementType = measurementTypeList.stream().filter(type -> type.getId().toString().equals(measurementTypeId)).findFirst();
      if(measurementType.isEmpty()) {
        log.error("Could not determine measurement type with id {}, skipping batch measurement", measurementTypeId);
        return;
      }
      EhrMapper ehrMapper = ehrMappers.get(measurementType.get().getMeasurementType().toString());
      String marshalledComposition = ehrMapper.marshallMeasurements(measurements, medicalDevice, measurementTypeList);
      measurementRepository.create(marshalledComposition, UUID.fromString(commands.getFirst().userId()));
    });
	}

	public List<Measurement> getMeasurementList(GetMeasurementsCommand command) {
    List<Measurement> ehrMeasurements = measurementRepository.findAll(command);
    List<Measurement> apiMeasurements = apiDeviceServiceWrapper.getMeasurements(command);

    Map<MeasurementKey, Measurement> mergedMeasurements = new LinkedHashMap<>();
    for (Measurement measurement : ehrMeasurements) {
      mergedMeasurements.put(measurement.toMeasurementKey(), measurement);
    }
    for (Measurement measurement : apiMeasurements) {
      mergedMeasurements.putIfAbsent(measurement.toMeasurementKey(), measurement);
    }

    return mergedMeasurements.values().stream()
        .sorted(Comparator.comparing(Measurement::measurementTime))
        .toList();
	}

  public MeasurementPage getMeasurementPage(GetMeasurementsCommand command, int offset, int limit) {
    int ehrLimit = (int) Math.min((long) offset + limit, Integer.MAX_VALUE);
    MeasurementPage ehrMeasurementPage = measurementRepository.findPage(command, 0, ehrLimit);
    List<Measurement> apiMeasurements = apiDeviceServiceWrapper.getMeasurements(command);

    Map<MeasurementKey, Measurement> mergedMeasurements = new LinkedHashMap<>();
    for (Measurement measurement : ehrMeasurementPage.measurements()) {
      mergedMeasurements.put(measurement.toMeasurementKey(), measurement);
    }
    for (Measurement measurement : apiMeasurements) {
      mergedMeasurements.putIfAbsent(measurement.toMeasurementKey(), measurement);
    }

    List<Measurement> page = mergedMeasurements.values().stream()
        .sorted(Comparator.comparing(Measurement::measurementTime))
        .skip(offset)
        .limit(limit)
        .toList();

    return new MeasurementPage(page, ehrMeasurementPage.totalElements());
  }
}
