package si.result.eearly.domain.api_device;

import java.time.OffsetDateTime;
import java.util.UUID;

import si.result.eearly.domain.task_queue.TaskQueue;

public record GetMeasurementCommand(
		UUID deviceId,
		UUID userId,
		UUID measurementTypeId,
		OffsetDateTime startTimestamp,
		OffsetDateTime endTimestamp) {

	static public GetMeasurementCommand fromTaskQueue(TaskQueue taskQueue) {
		return new GetMeasurementCommand(
				taskQueue.getDevice().getId(),
				taskQueue.getProfile().getUserId(),
				taskQueue.getMeasurementType().getId(),
				taskQueue.getStartTimestamp(),
				taskQueue.getEndTimestamp());
	}
}
