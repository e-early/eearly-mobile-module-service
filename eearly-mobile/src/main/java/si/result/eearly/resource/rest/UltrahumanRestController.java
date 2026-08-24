package si.result.eearly.resource.rest;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import si.result.eearly.dto.UltrahumanMetricsDTO;
import si.result.eearly.facade.UltrahumanServiceRestFacade;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sensors/ultrahuman")
public class UltrahumanRestController {
	private final UltrahumanServiceRestFacade ultrahumanService;

	@GetMapping("/metrics")
	public ResponseEntity<UltrahumanMetricsDTO> getMetrics(
			@RequestParam UUID deviceId,
			@RequestParam Long date) {
		return ResponseEntity.ok(ultrahumanService.getMetrics(deviceId, OffsetDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault())));
	}
}
