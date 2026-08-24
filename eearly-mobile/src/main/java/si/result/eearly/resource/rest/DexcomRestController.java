package si.result.eearly.resource.rest;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import si.result.eearly.dto.DexcomAlertsDTO;
import si.result.eearly.dto.DexcomCalibrationsDTO;
import si.result.eearly.dto.DexcomDataRangeDTO;
import si.result.eearly.dto.DexcomDevicesDTO;
import si.result.eearly.dto.DexcomEgvsDTO;
import si.result.eearly.dto.DexcomEventsDTO;
import si.result.eearly.facade.DexcomServiceRestFacade;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/sensors/dexcom")
public class DexcomRestController {

	private final DexcomServiceRestFacade dexcomService;

	@GetMapping("/alerts")
	public ResponseEntity<DexcomAlertsDTO> getAlerts(
			@RequestParam UUID deviceId,
			@RequestParam Long startDate,
			@RequestParam Long endDate) {

		return ResponseEntity.ok(dexcomService.getAlerts(
				deviceId,
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(startDate), ZoneId.systemDefault()),
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(endDate), ZoneId.systemDefault())));
	}

	@GetMapping("/calibrations")
	public ResponseEntity<DexcomCalibrationsDTO> getCalibrations(
			@RequestParam UUID deviceId,
			@RequestParam Long startDate,
			@RequestParam Long endDate) {

		return ResponseEntity.ok(dexcomService.getCalibrations(
				deviceId,
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(startDate), ZoneId.systemDefault()),
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(endDate), ZoneId.systemDefault())));
	}

	@GetMapping("/egvs")
	public ResponseEntity<DexcomEgvsDTO> getEgvs(
			@RequestParam UUID deviceId,
			@RequestParam Long startDate,
			@RequestParam Long endDate) {

		return ResponseEntity.ok(dexcomService.getEgvs(
				deviceId,
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(startDate), ZoneId.systemDefault()),
						OffsetDateTime.ofInstant(Instant.ofEpochMilli(endDate), ZoneId.systemDefault())));
	}

	@GetMapping("/dataRange")
	public ResponseEntity<DexcomDataRangeDTO> getDataRange(
			@RequestParam UUID deviceId,
			@RequestParam Long lastSyncTime) {

		return ResponseEntity.ok(dexcomService.getDataRange(
				deviceId,
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(lastSyncTime), ZoneId.systemDefault())));
	}

	@GetMapping("/devices")
	public ResponseEntity<DexcomDevicesDTO> getDevices(
			@RequestParam UUID deviceId) {

		return ResponseEntity.ok(dexcomService.getDevices(deviceId));
	}

	@GetMapping("/events")
	public ResponseEntity<DexcomEventsDTO> getEvents(
			@RequestParam UUID deviceId,
			@RequestParam Long startDate,
			@RequestParam Long endDate) {

		return ResponseEntity.ok(dexcomService.getEvents(
				deviceId,
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(startDate), ZoneId.systemDefault()),
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(endDate), ZoneId.systemDefault())));
	}
}
