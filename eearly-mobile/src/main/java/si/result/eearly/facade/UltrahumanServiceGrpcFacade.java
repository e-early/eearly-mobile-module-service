package si.result.eearly.facade;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics;
import si.result.eearly.genproto.GetMetricsRequest;
import si.result.eearly.genproto.GetMetricsResponse;
import si.result.eearly.mapper.UltrahumanMapper;
import si.result.eearly.service.ApiDeviceServiceUltrahumanImpl;
import si.result.eearly.service.DeviceUserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UltrahumanServiceGrpcFacade {

	// TODO: fix name
	private final String manufacturerName = "UltraHuman";

	private final DeviceUserService deviceUserService;
	private final ApiDeviceServiceUltrahumanImpl ultrahumanService;
	private final UltrahumanMapper ultrahumanMapper;

	public GetMetricsResponse getMetrics(GetMetricsRequest request) {
		String authCode = deviceUserService
				.getDeviceUser(UUID.fromString(request.getDeviceId()), manufacturerName)
				.getApiKey();

		UltrahumanMetrics metrics = ultrahumanService.getMetrics(
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getDate()), ZoneId.systemDefault()),
				authCode).getBody();

		return ultrahumanMapper.toMetricsGrpc(metrics);
	}
}
