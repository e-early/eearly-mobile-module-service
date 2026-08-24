package si.result.eearly.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import si.result.eearly.domain.api_device.CheckHeartbeatCommand;
import si.result.eearly.domain.api_device.GetMeasurementCommand;
import si.result.eearly.domain.api_device.Heartbeat;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.exception.NotFoundException;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics;

@Service("UltraHuman")
@RequiredArgsConstructor
@Slf4j
public class ApiDeviceServiceUltrahumanImpl implements ApiDeviceService {

	@Value("${sensor.ultrahuman.base-uri}")
	private String baseUri;
	@Value("${sensor.ultrahuman.authorization}")
	private String authorization;

	private final RestTemplate restTemplate;

	@Override
	public Heartbeat checkHeartbeat(CheckHeartbeatCommand command, DeviceUser deviceUser) {
		ResponseEntity<UltrahumanMetrics> metrics;

		Heartbeat output;
		try {
			metrics = getMetrics(OffsetDateTime.now(), deviceUser.getApiKey());
			output = new Heartbeat(metrics.getBody().getStatus(), metrics.getBody().getError());
		} catch (HttpClientErrorException e) {
			output = new Heartbeat(e.getStatusCode().value(), e.getResponseBodyAsString());
		} catch (Exception e) {
			output = new Heartbeat(500, e.getMessage());
		}

		return output;
	}

	@Override
	public List<Measurement> getMeasurements(GetMeasurementCommand command, DeviceUser deviceUser,
			MeasurementType measurementType) {
		UltrahumanMetrics metrics = getMetrics(command.startTimestamp(), deviceUser.getApiKey()).getBody();

		var startDate = command.startTimestamp().truncatedTo(ChronoUnit.DAYS);
		var endDate = command.endTimestamp().truncatedTo(ChronoUnit.DAYS);
		if (!startDate.equals(endDate)) {
			log.debug("Over spill detected, fetching next day metrics");
			UltrahumanMetrics nextDayMetrics = getMetrics(command.endTimestamp(), deviceUser.getApiKey()).getBody();

			metrics.getData().mergeMetrics(nextDayMetrics.getData());
		}

		if (metrics.getData() == null) {
			return List.of();
		}

		List<Measurement> measurements = metrics.getData().getMetricData().stream()
				.filter(data -> measurementType.getMeasurementType() == data.getMeasurementType())
				.findFirst()
				.map(data -> {
					if (data.getObject() == null) {
						log.warn("Object is null for MeasurementType {}", command.measurementTypeId());
						return new ArrayList<Measurement>();
					}

					if (!(data.getObject() instanceof UltrahumanMetrics.UltrahumanObjectBase)) {
						log.warn("Object is not an instance of UltrahumanObjectBase for MeasurementType {}. Object type: {}",
								command.measurementTypeId(), data.getObject().getClass().getName());
						return new ArrayList<Measurement>();
					}

					return ((UltrahumanMetrics.UltrahumanObjectBase) data.getObject()).toMeasurements(deviceUser, measurementType);
				})
				.orElseThrow(() -> {
					log.error("No measurement found for MeasurementType {}", command.measurementTypeId());
					return new NotFoundException("No measurement found for MeasurementType " + command.measurementTypeId());
				});

		measurements = measurements.stream()
				.filter(m -> m.measurementTime().isAfter(command.startTimestamp().toZonedDateTime()) && m.measurementTime().isBefore(
            command.endTimestamp().toZonedDateTime()))
				.collect(Collectors.toList());

    return measurements;
	}

	public ResponseEntity<UltrahumanMetrics> getMetrics(OffsetDateTime date, String email) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, authorization);
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

		Map<String, Object> params = new HashMap<>();
		params.put("email", email);
		params.put("date", date.format(DateTimeFormatter.ISO_DATE));

		String url = String.format("%s/api/v1/metrics?email={email}&date={date}", baseUri);
		log.debug("Fetching Ultrahuman metrics from URL: {} with params: {}", url, params);

		ResponseEntity<UltrahumanMetrics> result;
		try {

			result = restTemplate.exchange(
					url,
					HttpMethod.GET,
					entity,
					UltrahumanMetrics.class,
					params);

		} catch (HttpClientErrorException e) {
			log.error("HTTP error while getting metrics for email {} on date {}: {} - {}", email, date, e.getStatusCode(),
					e.getResponseBodyAsString());
			throw e;
		} catch (RestClientException e) {
			log.error("Rest client error while getting metrics for email {} on date {}. URL: {}", email, date, url);
			try {
				ResponseEntity<String> rawResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, params);
				log.error("Raw response from Ultrahuman API: {}", rawResponse.getBody());
			} catch (Exception ex) {
				log.error("Could not fetch raw response for debugging", ex);
			}
			throw e;
		}

		return result;
	}
}
