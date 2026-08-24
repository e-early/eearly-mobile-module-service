package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import si.result.eearly.domain.api_device.GetMeasurementCommand;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.enums.TaskQueueStatus;
import si.result.eearly.domain.exception.NotFoundException;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.domain.task_queue.TaskQueue;
import si.result.eearly.repository.DeviceUserRepository;
import si.result.eearly.repository.TaskQueueRepository;
import si.result.eearly.service.postgres.LockingManager;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskQueueServiceImpl implements TaskQueueService {

	private final TransactionTemplate transactionTemplate;
	private final LockingManager lockingManager;

	private final TaskQueueRepository taskQueueRepository;
	private final DeviceUserRepository deviceUserRepository;
	private final Map<String, ApiDeviceService> apiDeviceImplementations;

	@Value("${taskqueue.retry.count}")
	private Integer retryCount;

	@Value("${taskqueue.retry.delay.seconds}")
	private Integer retryDelaySeconds;

	@Override
	public void processTasks() {

		List<TaskQueue> tasks = taskQueueRepository.findByStatusOrStatusAndRetryAtIsBefore(TaskQueueStatus.TODO, TaskQueueStatus.RETRY, OffsetDateTime.now());

		for (TaskQueue taskQueue : tasks) {
			log.info("Processing task from queue: {}", taskQueue.getId());

			lockingManager.withLock(taskQueue.getId().toString(), () -> {
				updateTaskQueueWithStatus(taskQueue, TaskQueueStatus.PROCESSING);

				try {
					transactionTemplate.executeWithoutResult(status -> {
						DeviceUser deviceUser = deviceUserRepository
								.findByUserAndDevice(
										taskQueue.getProfile().getUserId(),
										taskQueue.getDevice().getId())
								.orElseThrow(() -> new RuntimeException("DeviceUser not found"));

						MeasurementType measurementType = deviceUser.getDevice().getMeasurementTypeList().stream()
								.filter(mt -> mt.getId().equals(taskQueue.getMeasurementType().getId()))
								.findAny()
								.orElseThrow(() -> new NotFoundException("MeasurementType not found"));

						ApiDeviceService service = apiDeviceImplementations.get(deviceUser
								.getDevice()
								.getManufacturer()
								.getName());

						List<Measurement> measurements = service.getMeasurements(
								GetMeasurementCommand.fromTaskQueue(taskQueue),
								deviceUser,
								measurementType);

						if (measurements.isEmpty()) {
							log.info("No measurements received, retrying in 5 seconds");
							throw new NotFoundException("No measurements received");
						} else {
							log.info("Task done, deleting from queue");
							taskQueueRepository.delete(taskQueue);
						}
					});
				} catch (NotFoundException e) {
					setTaskToRetry(taskQueue);
					throw e;
				} catch (Exception e) {
					setTaskToRetry(taskQueue);
					throw e;
				}
			});
		}
	}

	private void setTaskToRetry(TaskQueue taskQueue) {
		taskQueue.incrementRetryCount();
		taskQueue.setRetryAtWithRetryDelay(retryDelaySeconds);

		updateTaskQueueWithStatus(taskQueue,
				taskQueue.getRetryCount() >= retryCount ? TaskQueueStatus.FAILED : TaskQueueStatus.RETRY);

		if (taskQueue.getRetryCount() >= retryCount) {
			log.error("Task failed after {} retries", retryCount);
		} else {
			log.warn("Task failed, retrying...");
		}
	}

	private void updateTaskQueueWithStatus(TaskQueue taskQueue, TaskQueueStatus status) {
		taskQueue.setStatus(status);
		taskQueueRepository.save(taskQueue);
	}
}
