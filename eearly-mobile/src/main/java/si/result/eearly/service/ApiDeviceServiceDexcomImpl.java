package si.result.eearly.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import si.result.eearly.domain.api_device.CheckHeartbeatCommand;
import si.result.eearly.domain.api_device.GetMeasurementCommand;
import si.result.eearly.domain.api_device.Heartbeat;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.dexcom.DexcomAlerts;
import si.result.eearly.domain.dexcom.DexcomCalibrations;
import si.result.eearly.domain.dexcom.DexcomDataRange;
import si.result.eearly.domain.dexcom.DexcomDevices;
import si.result.eearly.domain.dexcom.DexcomEgvs;
import si.result.eearly.domain.dexcom.DexcomEvents;
import si.result.eearly.domain.dexcom.DexcomToken;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.repository.MeasurementRepository;

@Service("Dexcom")
@RequiredArgsConstructor
@Slf4j
public class ApiDeviceServiceDexcomImpl implements ApiDeviceService {

	@Value("${sensor.dexcom.base-uri}")
	private String baseUri;
	@Value("${sensor.dexcom.redirect-uri}")
	private String redirectUri;
	@Value("${sensor.dexcom.client.id}")
	private String clientId;
	@Value("${sensor.dexcom.client.secret}")
	private String clientSecret;
	@Value("${measurement.api-device.dexcom.max-range-days:30}")
	private Long maxRangeDays;

	private final CacheManager cacheManager;
	private final RestTemplate restTemplate;

	@Getter
  private final MeasurementRepository measurementRepository;

	// Unfortunately can't use Cacheable annotation here, because expiration token
	// logic is too complicated
	public DexcomToken getToken(String authCode) {
		Cache cache = cacheManager.getCache("dexcomToken");
		DexcomToken token = cache != null ? cache.get(authCode, DexcomToken.class) : null;

		try {
			if (token == null) {
				log.info("No cached token. Requesting...");

				token = requestToken(getAuthCodeParameters(authCode));
			} else if (token.getPayload().isExpired()) {
				try {
					log.info("Token was expired. Trying to fetch by refresh token");

					token = requestToken(getRefreshTokenParameters(token));
				} catch (HttpClientErrorException e) {
					log.error("Error while refreshing token", e);

					token = requestToken(getAuthCodeParameters(authCode));
				}

			} else {
				log.info("Token is cached and valid");

			}
		} catch (JsonProcessingException e) {
			log.error("Cannot decode payload", e);
		}

    assert cache != null;
    cache.put(authCode, token);
		return token;
	}

	private MultiValueMap<String, String> getAuthCodeParameters(String authCode) {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("grant_type", "authorization_code");
		parameters.add("code", authCode);
		parameters.add("redirect_uri", redirectUri);
		parameters.add("client_id", clientId);
		parameters.add("client_secret", clientSecret);
		return parameters;
	}

	private MultiValueMap<String, String> getRefreshTokenParameters(DexcomToken token) {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("grant_type", "refresh_token");
		parameters.add("refresh_token", token.getRefreshToken());
		parameters.add("redirect_uri", redirectUri);
		parameters.add("client_id", clientId);
		parameters.add("client_secret", clientSecret);
		return parameters;
	}

	private DexcomToken requestToken(MultiValueMap<String, String> parameters) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameters, headers);

		ResponseEntity<DexcomToken> result;
		try {
			result = restTemplate.exchange(
					String.format("%s/v2/oauth2/token", baseUri),
					HttpMethod.POST,
					entity,
					DexcomToken.class);
		} catch (HttpClientErrorException e) {
			log.error("Error while getting token", e);
			throw e;
		}

		return result.getBody();
	}

	private HttpEntity<MultiValueMap<String, String>> getHttpEntity(String authCode) {
		DexcomToken token = getToken(authCode);
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, token.getBearer());

		return new HttpEntity<>(headers);
	}

	private void validateStartEndDate(OffsetDateTime startDate, OffsetDateTime endDate) {
		if (startDate == null || endDate == null) {
			throw new IllegalArgumentException("Start and end date must be provided");
		}
		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("Start date must be before end date");
		}
	}

	@Override
	public Heartbeat checkHeartbeat(CheckHeartbeatCommand command, DeviceUser deviceUser) {
		ResponseEntity<DexcomDevices> devices;

    Heartbeat output;
		try {
			devices = getDevices(deviceUser.getApiKey());
			output = new Heartbeat(devices.getStatusCode().value(), null);
		} catch (HttpClientErrorException e) {
			output = new Heartbeat(e.getStatusCode().value(), e.getResponseBodyAsString());
		} catch (Exception e) {
			output = new Heartbeat(500, e.getMessage());
		}

		return output;
	}

	@Override
	public List<Measurement> getMeasurements(GetMeasurementCommand command, DeviceUser deviceUser, MeasurementType measurementType) {
		List<Measurement> measurements = new ArrayList<>();
		OffsetDateTime rangeStart = command.startTimestamp();
		long rangeDays = Math.max(maxRangeDays, 1);

		while (rangeStart.isBefore(command.endTimestamp())) {
			OffsetDateTime rangeEnd = rangeStart.plusDays(rangeDays);
			if (rangeEnd.isAfter(command.endTimestamp())) {
				rangeEnd = command.endTimestamp();
			}

			DexcomEgvs egvs = getEgvs(deviceUser.getApiKey(), rangeStart, rangeEnd);
			if (egvs != null && egvs.getRecords() != null && !egvs.getRecords().isEmpty()) {
				measurements.addAll(egvs
						.getRecords()
						.stream()
						.map(record -> record.toMeasurement(deviceUser, measurementType))
						.toList());
			}

			rangeStart = rangeEnd;
		}

		return measurements;
	}

	public DexcomAlerts getAlerts(String authCode, OffsetDateTime startDate, OffsetDateTime endDate) {
		validateStartEndDate(startDate, endDate);

		Map<String, Object> params = new HashMap<>();
		params.put("startDate", formatOffsetDateTime(startDate));
		params.put("endDate", formatOffsetDateTime(endDate));

		ResponseEntity<DexcomAlerts> result;
		try {
			result = restTemplate.exchange(
					String.format("%s/v3/users/self/alerts?startDate={startDate}&endDate={endDate}", baseUri),
					HttpMethod.GET,
					getHttpEntity(authCode),
					DexcomAlerts.class,
					params);
		} catch (HttpClientErrorException e) {
			log.error("Error while getting alerts", e);
			throw e;
		}

    return result.getBody();
	}

	public DexcomCalibrations getCalibrations(String authCode, OffsetDateTime startDate, OffsetDateTime endDate) {
		validateStartEndDate(startDate, endDate);

		Map<String, Object> params = new HashMap<>();
		params.put("startDate", formatOffsetDateTime(startDate));
		params.put("endDate", formatOffsetDateTime(endDate));

		ResponseEntity<DexcomCalibrations> result;
		try {
			result = restTemplate.exchange(
					String.format("%s/v3/users/self/calibrations?startDate={startDate}&endDate={endDate}", baseUri),
					HttpMethod.GET,
					getHttpEntity(authCode),
					DexcomCalibrations.class,
					params);
		} catch (HttpClientErrorException e) {
			log.error("Error while getting calibrations", e);
			throw e;
		}

    return result.getBody();
	}

	public DexcomEgvs getEgvs(String authCode, OffsetDateTime startDate, OffsetDateTime endDate) {
		validateStartEndDate(startDate, endDate);

		Map<String, Object> params = new HashMap<>();
		params.put("startDate", formatOffsetDateTime(startDate));
		params.put("endDate", formatOffsetDateTime(endDate));

		ResponseEntity<DexcomEgvs> result;
		try {
			result = restTemplate.exchange(
					String.format("%s/v3/users/self/egvs?startDate={startDate}&endDate={endDate}", baseUri),
					HttpMethod.GET,
					getHttpEntity(authCode),
					DexcomEgvs.class,
					params);
		} catch (HttpClientErrorException e) {
			log.error("Error while getting egvs", e);
			throw e;
		}

    return result.getBody();
	}

	public DexcomDataRange getDataRange(String authCode, OffsetDateTime lastSyncTime) {
		String formattedLastSyncTime = DateTimeFormatter.ISO_INSTANT.format(lastSyncTime.toInstant());
		String uri = UriComponentsBuilder
				.fromHttpUrl(String.format("%s/v3/users/self/dataRange", baseUri))
				.queryParam("lastSyncTime", formattedLastSyncTime)
				.encode()
				.toUriString();

		ResponseEntity<DexcomDataRange> result;
		try {
			result = restTemplate.exchange(
					uri,
					HttpMethod.GET,
					getHttpEntity(authCode),
					DexcomDataRange.class);
		} catch (HttpClientErrorException e) {
			log.error("Error while getting data range", e);
			throw e;
		}

    return result.getBody();
	}

	public ResponseEntity<DexcomDevices> getDevices(String authCode) {
		ResponseEntity<DexcomDevices> result;
		try {
			result = restTemplate.exchange(
					String.format("%s/v3/users/self/devices", baseUri),
					HttpMethod.GET,
					getHttpEntity(authCode),
					DexcomDevices.class);
		} catch (HttpClientErrorException e) {
			log.error("Error while getting devices", e);
			throw e;
		}

		// DexcomDevices devices = result.getBody();

		return result;
	}

	public DexcomEvents getEvents(String authCode, OffsetDateTime startDate, OffsetDateTime endDate) {
		validateStartEndDate(startDate, endDate);

		Map<String, Object> params = new HashMap<>();
		params.put("startDate", formatOffsetDateTime(startDate));
		params.put("endDate", formatOffsetDateTime(endDate));

		ResponseEntity<DexcomEvents> result;
		try {
			result = restTemplate.exchange(
					String.format("%s/v3/users/self/events?startDate={startDate}&endDate={endDate}", baseUri),
					HttpMethod.GET,
					getHttpEntity(authCode),
					DexcomEvents.class,
					params);
		} catch (HttpClientErrorException e) {
			log.error("Error while getting events", e);
			throw e;
		}

    return result.getBody();
	}

  private String formatOffsetDateTime(OffsetDateTime offsetDateTime) {
    String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    return  DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).format(offsetDateTime);
	}

}
