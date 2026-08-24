package si.result.eearly.service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import si.result.eearly.domain.api_device.CheckHeartbeatCommand;
import si.result.eearly.domain.api_device.GetMeasurementCommand;
import si.result.eearly.domain.api_device.Heartbeat;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.exception.NotFoundException;
import si.result.eearly.domain.measurement.GetMeasurementsCommand;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.domain.task_queue.TaskQueue;
import si.result.eearly.repository.DeviceUserRepository;
import si.result.eearly.repository.TaskQueueRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ApiDeviceServiceWrapper {

	private final DeviceUserRepository deviceUserRepository;
	private final TaskQueueRepository taskQueueRepository;
	private final Map<String, ApiDeviceService> apiDeviceImplementations;

	@Value("${measurement.api-device.default-lookback-days:30}")
	private Long apiDeviceDefaultLookbackDays;

	public Heartbeat checkHeartbeat(CheckHeartbeatCommand command) {
		DeviceUser deviceUser = deviceUserRepository
				.findByUserAndDevice(command.userId(), command.deviceId())
				.orElseThrow(() -> new RuntimeException("Device User not found"));

		return checkHeartbeat(deviceUser);
	}

	public Heartbeat checkHeartbeat(DeviceUser deviceUser) {
		log.debug("Api Device Implementations: {}", apiDeviceImplementations);

		ApiDeviceService service = apiDeviceImplementations.get(deviceUser
						.getDevice()
						.getManufacturer()
						.getName());
		CheckHeartbeatCommand command = new CheckHeartbeatCommand(
						deviceUser.getDevice().getId(),
						deviceUser.getProfile().getUserId()
		);
		return service.checkHeartbeat(command, deviceUser);
	}

	public List<Measurement> getMeasurement(GetMeasurementCommand command) {
		DeviceUser deviceUser = deviceUserRepository
				.findByUserAndDevice(command.userId(), command.deviceId())
				.orElseThrow(() -> new NotFoundException("DeviceUser not found"));

		MeasurementType measurementType = deviceUser.getDevice().getMeasurementTypeList().stream()
				.filter(mt -> mt.getId().equals(command.measurementTypeId()))
				.findAny()
				.orElseThrow(() -> new NotFoundException("MeasurementType not found"));

		return getMeasurements(command, deviceUser, measurementType);
	}

	public List<Measurement> getMeasurements(
			DeviceUser deviceUser,
			MeasurementType measurementType,
			OffsetDateTime startTimestamp,
			OffsetDateTime endTimestamp) {
		return getMeasurements(
				new GetMeasurementCommand(
						deviceUser.getDevice().getId(),
						deviceUser.getProfile().getUserId(),
						measurementType.getId(),
						startTimestamp,
						endTimestamp),
				deviceUser,
				measurementType);
	}

	public List<Measurement> getMeasurements(GetMeasurementsCommand command) {
		OffsetDateTime endTimestamp = parseOffsetDateTime(command.endDateTime());
		if (endTimestamp == null) {
			endTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
		}

		OffsetDateTime startTimestamp = parseOffsetDateTime(command.startDateTime());
		if (startTimestamp == null) {
			startTimestamp = endTimestamp.minusDays(apiDeviceDefaultLookbackDays);
		}

		var deviceUsers = deviceUserRepository.findAllByPrimaryKeyProfileUserId(command.ehrId());
		if (deviceUsers.isEmpty()) {
			return List.of();
		}

		List<Measurement> measurements = new ArrayList<>();
		for (var deviceUser : deviceUsers) {
			List<MeasurementType> deviceMeasurementTypes = deviceUser.getDevice().getMeasurementTypeList();
			if (deviceMeasurementTypes == null || deviceMeasurementTypes.isEmpty()) {
				continue;
			}

			for (MeasurementType measurementType : filterRequestedMeasurementTypes(deviceMeasurementTypes, command)) {
				try {
					measurements.addAll(getMeasurements(
							deviceUser,
							measurementType,
							startTimestamp,
							endTimestamp));
				} catch (Exception e) {
					log.warn("Could not fetch API measurements for user {}, device {}, measurement type {}: {}",
							command.ehrId(),
							deviceUser.getDevice().getId(),
							measurementType.getId(),
							e.getMessage());
				}
			}
		}

		return measurements;
	}

	private List<MeasurementType> filterRequestedMeasurementTypes(
			List<MeasurementType> deviceMeasurementTypes,
			GetMeasurementsCommand command) {
		if (command.observationIds() == null || command.observationIds().isEmpty()) {
			return deviceMeasurementTypes;
		}

		return deviceMeasurementTypes.stream()
				.filter(measurementType -> command.observationIds().contains(measurementType.getObservationId()))
				.toList();
	}

	private OffsetDateTime parseOffsetDateTime(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		try {
			return OffsetDateTime.parse(value);
		} catch (Exception ignored) {
			return ZonedDateTime.parse(value).toOffsetDateTime();
		}
	}

	private List<Measurement> getMeasurements(
			GetMeasurementCommand command,
			DeviceUser deviceUser,
			MeasurementType measurementType) {
		ApiDeviceService service = apiDeviceImplementations.get(deviceUser
				.getDevice()
				.getManufacturer()
				.getName());

		if (service == null) {
			throw new NotFoundException("ApiDeviceService not found");
		}

		List<Measurement> measurements = service.getMeasurements(command, deviceUser, measurementType);

		if (measurements.isEmpty()) {
			log.warn("No data found for metrics, adding task to queue");

			TaskQueue newTaskQueue = TaskQueue.create(deviceUser, measurementType, command);

			Optional<TaskQueue> taskQueue = taskQueueRepository.findOne(Example.of(
					newTaskQueue,
					ExampleMatcher
							.matching()
							.withIgnorePaths("id", "status", "retryCount", "createdAt")));

			if (taskQueue.isEmpty()) {
				log.info("Adding task to queue: {}", newTaskQueue.getId());
				taskQueueRepository.save(newTaskQueue);
			} else {
				log.warn("Task already exists in queue: {}", taskQueue.get().getId());
			}
		}

		return measurements;
	}
}
