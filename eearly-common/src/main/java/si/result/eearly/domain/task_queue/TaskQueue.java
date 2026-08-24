package si.result.eearly.domain.task_queue;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import si.result.eearly.domain.api_device.GetMeasurementCommand;
import si.result.eearly.domain.device.Device;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.enums.TaskQueueStatus;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.domain.profile.Profile;

@Entity
@Table(name = "task_queue")
@RequiredArgsConstructor
@Getter
@Setter
public final class TaskQueue {

	@Id
	@Column(name = "id")
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Profile profile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "device_id", nullable = false)
	private Device device;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "measurement_type_id", nullable = false)
	private MeasurementType measurementType;

	@Column(name = "start_time", nullable = false)
	private OffsetDateTime startTimestamp;

	@Column(name = "end_time", nullable = false)
	private OffsetDateTime endTimestamp;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private TaskQueueStatus status;

	@Column(name = "retry_count", nullable = false)
	private Integer retryCount;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "retry_at")
	private OffsetDateTime retryAt;

	public Integer incrementRetryCount() {
		setRetryCount(getRetryCount() + 1);
		return getRetryCount();
	}

	public OffsetDateTime setRetryAtWithRetryDelay(Integer retryDelaySeconds) {
		setRetryAt(OffsetDateTime.now().plusSeconds(getRetryCount() * retryDelaySeconds));
		return getRetryAt();
	}

	static public TaskQueue create(DeviceUser deviceUser, MeasurementType measurementType,
			GetMeasurementCommand command) {
		TaskQueue taskQueue = new TaskQueue();
		taskQueue.setProfile(deviceUser.getProfile());
		taskQueue.setDevice(deviceUser.getDevice());
		taskQueue.setMeasurementType(measurementType);
		taskQueue.setStartTimestamp(command.startTimestamp());
		taskQueue.setEndTimestamp(command.endTimestamp());
		taskQueue.setStatus(TaskQueueStatus.TODO);
		taskQueue.setRetryCount(0);
		taskQueue.setCreatedAt(OffsetDateTime.now());
		return taskQueue;
	}
}
