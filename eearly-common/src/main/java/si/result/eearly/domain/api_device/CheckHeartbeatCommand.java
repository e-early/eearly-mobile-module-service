package si.result.eearly.domain.api_device;

import java.util.UUID;

public record CheckHeartbeatCommand(
		UUID deviceId,
		UUID userId) {
}
