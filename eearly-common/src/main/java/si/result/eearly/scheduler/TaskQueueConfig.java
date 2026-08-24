package si.result.eearly.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import lombok.RequiredArgsConstructor;
import si.result.eearly.service.TaskQueueService;

@Configuration
@RequiredArgsConstructor
public class TaskQueueConfig implements SchedulingConfigurer {

    private final TaskQueueService taskQueueService;

    @Value("${taskqueue.cron.expression}")
    private String cronExpression;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addCronTask(taskQueueService::processTasks, cronExpression);
    }
}
