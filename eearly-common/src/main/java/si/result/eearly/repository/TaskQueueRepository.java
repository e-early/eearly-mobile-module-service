package si.result.eearly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.result.eearly.domain.enums.TaskQueueStatus;
import si.result.eearly.domain.task_queue.TaskQueue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskQueueRepository extends JpaRepository<TaskQueue, UUID> {

    List<TaskQueue> findByStatusOrStatusAndRetryAtIsBefore(TaskQueueStatus status, TaskQueueStatus status2, OffsetDateTime now);
}
