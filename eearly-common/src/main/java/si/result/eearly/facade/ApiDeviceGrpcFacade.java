package si.result.eearly.facade;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import si.result.eearly.domain.UserPrincipal;
import si.result.eearly.domain.api_device.Heartbeat;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.genproto.CheckHeartbeatRequest;
import si.result.eearly.genproto.CheckHeartbeatResponse;
import si.result.eearly.genproto.GetDexcomAuthorizationUriRequest;
import si.result.eearly.genproto.GetDexcomAuthorizationUriResponse;
import si.result.eearly.genproto.GetMeasurementRequest;
import si.result.eearly.genproto.GetMeasurementResponse;
import si.result.eearly.mapper.ApiDeviceMapper;
import si.result.eearly.mapper.MeasurementMapper;
import si.result.eearly.service.ApiDeviceServiceWrapper;
import si.result.eearly.service.UserPrincipalService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiDeviceGrpcFacade {

	private static final String DEXCOM_AUTHORIZATION_PATH = "/v3/oauth2/login";

	private final UserPrincipalService userService;
	private final ApiDeviceServiceWrapper apiDeviceService;
	private final ApiDeviceMapper apiDeviceMapper;
	private final MeasurementMapper measurementMapper;

	@Value("${sensor.dexcom.base-uri}")
	private String dexcomBaseUri;

	@Value("${sensor.dexcom.redirect-uri}")
	private String dexcomRedirectUri;

	@Value("${sensor.dexcom.client.id}")
	private String dexcomClientId;

	public CheckHeartbeatResponse checkHeartbeat(CheckHeartbeatRequest request) {
		UserPrincipal user = userService.getUserPrincipal();

		Heartbeat checkHeartbeat = apiDeviceService
				.checkHeartbeat(apiDeviceMapper.toCheckHeartbeatCommand(request.getDeviceId(), user.getId()));

		CheckHeartbeatResponse.Builder builder = CheckHeartbeatResponse.newBuilder()
				.setCode(checkHeartbeat.code());

		if (checkHeartbeat.error() != null) {
			builder.setError(checkHeartbeat.error());
		}

		return builder.build();
	}

	public GetMeasurementResponse getMeasurement(GetMeasurementRequest request) {
		UserPrincipal user = userService.getUserPrincipal();

		log.debug("Start date: {}", request.getStartDate());
		log.debug("End date: {}", request.getEndDate());

		List<Measurement> measurements = apiDeviceService
				.getMeasurement(apiDeviceMapper.toGetMeasurementCommand(request.getDeviceId(), user.getId(),
						request.getMeasurementTypeId(), request.getStartDate(), request.getEndDate()));

		return GetMeasurementResponse.newBuilder()
				.addAllMeasurements(measurements.stream()
						.map(value -> measurementMapper.toGrpcMeasurement(value))
						.collect(Collectors.toList()))
				.build();
	}

	public GetDexcomAuthorizationUriResponse getDexcomAuthorizationUri(GetDexcomAuthorizationUriRequest request) {
		String authorizationUri = UriComponentsBuilder
				.fromUriString(dexcomBaseUri)
				.replacePath(DEXCOM_AUTHORIZATION_PATH)
				.replaceQuery(null)
				.queryParam("client_id", encodeQueryParam(dexcomClientId))
				.queryParam("redirect_uri", encodeQueryParam(dexcomRedirectUri))
				.queryParam("response_type", "code")
				.queryParam("scope", "offline_access")
				.queryParam("state", encodeQueryParam(request.getState()))
				.build(true)
				.toUriString();

		return GetDexcomAuthorizationUriResponse.newBuilder()
				.setAuthorizationUri(authorizationUri)
				.setRedirectUri(dexcomRedirectUri)
				.build();
	}

	private String encodeQueryParam(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
