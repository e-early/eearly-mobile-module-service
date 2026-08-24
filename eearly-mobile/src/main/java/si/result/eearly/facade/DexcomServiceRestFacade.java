package si.result.eearly.facade;

import lombok.RequiredArgsConstructor;
import si.result.eearly.domain.dexcom.DexcomAlerts;
import si.result.eearly.domain.dexcom.DexcomCalibrations;
import si.result.eearly.domain.dexcom.DexcomDataRange;
import si.result.eearly.domain.dexcom.DexcomDevices;
import si.result.eearly.domain.dexcom.DexcomEgvs;
import si.result.eearly.domain.dexcom.DexcomEvents;
import si.result.eearly.dto.DexcomAlertsDTO;
import si.result.eearly.dto.DexcomCalibrationsDTO;
import si.result.eearly.dto.DexcomDataRangeDTO;
import si.result.eearly.dto.DexcomDevicesDTO;
import si.result.eearly.dto.DexcomEgvsDTO;
import si.result.eearly.dto.DexcomEventsDTO;
import si.result.eearly.mapper.DexcomMapper;
import si.result.eearly.service.ApiDeviceServiceDexcomImpl;
import si.result.eearly.service.DeviceUserService;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DexcomServiceRestFacade {

	private final String manufacturerName = "Dexcom";

	private final DeviceUserService deviceUserService;
	private final ApiDeviceServiceDexcomImpl dexcomService;
	private final DexcomMapper dexcomMapper;

	public DexcomAlertsDTO getAlerts(UUID deviceId, OffsetDateTime startDate, OffsetDateTime endDate) {
		String authCode = deviceUserService
				.getDeviceUser(deviceId, manufacturerName)
				.getApiKey();

		DexcomAlerts alerts = dexcomService.getAlerts(authCode, startDate, endDate);

		return dexcomMapper.toAlertsDTO(alerts);
	}

	public DexcomCalibrationsDTO getCalibrations(UUID deviceId, OffsetDateTime startDate, OffsetDateTime endDate) {
		String authCode = deviceUserService
				.getDeviceUser(deviceId, manufacturerName)
				.getApiKey();

		DexcomCalibrations calibrations = dexcomService.getCalibrations(authCode, startDate, endDate);

		return dexcomMapper.toCalibrationsDTO(calibrations);
	}

	public DexcomEgvsDTO getEgvs(UUID deviceId, OffsetDateTime startDate, OffsetDateTime endDate) {
		String authCode = deviceUserService
				.getDeviceUser(deviceId, manufacturerName)
				.getApiKey();

		DexcomEgvs egvs = dexcomService.getEgvs(authCode, startDate, endDate);

		return dexcomMapper.toEgvsDTO(egvs);
	}

	public DexcomDataRangeDTO getDataRange(UUID deviceId, OffsetDateTime lastSyncTime) {
		String authCode = deviceUserService
				.getDeviceUser(deviceId, manufacturerName)
				.getApiKey();

		DexcomDataRange dataRange = dexcomService.getDataRange(authCode, lastSyncTime);

		return dexcomMapper.toDataRangeDTO(dataRange);
	}

	public DexcomDevicesDTO getDevices(UUID deviceId) {
		String authCode = deviceUserService
				.getDeviceUser(deviceId, manufacturerName)
				.getApiKey();

		DexcomDevices devices = dexcomService.getDevices(authCode).getBody();

		return dexcomMapper.toDevicesDTO(devices);
	}

	public DexcomEventsDTO getEvents(UUID deviceId, OffsetDateTime startDate, OffsetDateTime endDate) {
		String authCode = deviceUserService
				.getDeviceUser(deviceId, manufacturerName)
				.getApiKey();

		DexcomEvents events = dexcomService.getEvents(authCode, startDate, endDate);

		return dexcomMapper.toEventsDTO(events);
	}
}
