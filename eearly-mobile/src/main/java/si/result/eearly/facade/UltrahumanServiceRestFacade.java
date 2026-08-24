package si.result.eearly.facade;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics;
import si.result.eearly.dto.UltrahumanMetricsDTO;
import si.result.eearly.mapper.UltrahumanMapper;
import si.result.eearly.service.ApiDeviceServiceUltrahumanImpl;
import si.result.eearly.service.DeviceUserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UltrahumanServiceRestFacade {

	// TODO: fix name
	private final String manufacturerName = "UltraHuman";

	private final DeviceUserService deviceUserService;
	private final ApiDeviceServiceUltrahumanImpl ultrahumanService;
	private final UltrahumanMapper ultrahumanMapper;

	public UltrahumanMetricsDTO getMetrics(UUID deviceId, OffsetDateTime date) {
		String authCode = deviceUserService
				.getDeviceUser(deviceId, manufacturerName)
				.getApiKey();

		UltrahumanMetrics metrics = ultrahumanService.getMetrics(date, authCode).getBody();

		return ultrahumanMapper.toMetricsDTO(metrics);
	}
}
