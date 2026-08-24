package si.result.eearly.facade;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import si.result.eearly.domain.dexcom.DexcomAlerts;
import si.result.eearly.domain.dexcom.DexcomCalibrations;
import si.result.eearly.domain.dexcom.DexcomDataRange;
import si.result.eearly.domain.dexcom.DexcomDevices;
import si.result.eearly.domain.dexcom.DexcomEgvs;
import si.result.eearly.domain.dexcom.DexcomEvents;
import si.result.eearly.mapper.DexcomMapper;
import si.result.eearly.genproto.GetAlertsResponse;
import si.result.eearly.genproto.GetCalibrationsResponse;
import si.result.eearly.genproto.GetEgvsResponse;
import si.result.eearly.genproto.GetDataRangeResponse;
import si.result.eearly.genproto.GetDevicesResponse;
import si.result.eearly.genproto.GetEventsResponse;
import si.result.eearly.genproto.GetDevicesRequest;
import si.result.eearly.genproto.GetAlertsRequest;
import si.result.eearly.genproto.GetEventsRequest;
import si.result.eearly.genproto.GetDataRangeRequest;
import si.result.eearly.genproto.GetCalibrationsRequest;
import si.result.eearly.genproto.GetEgvsRequest;
import si.result.eearly.service.ApiDeviceServiceDexcomImpl;
import si.result.eearly.service.DeviceUserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DexcomServiceGrpcFacade {

	private final String manufacturerName = "Dexcom";

	private final DeviceUserService deviceUserService;
	private final ApiDeviceServiceDexcomImpl dexcomService;
	private final DexcomMapper dexcomMapper;

	public GetAlertsResponse getAlerts(GetAlertsRequest request) {
		String authCode = deviceUserService
				.getDeviceUser(UUID.fromString(request.getDeviceId()), manufacturerName)
				.getApiKey();

		DexcomAlerts alerts = dexcomService.getAlerts(
				authCode,
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getStartDate()), ZoneId.systemDefault()),
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getEndDate()), ZoneId.systemDefault()));
		return dexcomMapper.toAlertsGrpc(alerts);
	}

	public GetCalibrationsResponse getCalibrations(GetCalibrationsRequest request) {
		String authCode = deviceUserService
				.getDeviceUser(UUID.fromString(request.getDeviceId()), manufacturerName)
				.getApiKey();

		DexcomCalibrations calibrations = dexcomService.getCalibrations(
				authCode,
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getStartDate()), ZoneId.systemDefault()),
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getEndDate()), ZoneId.systemDefault()));
		return dexcomMapper.toCalibrationsGrpc(calibrations);
	}

	public GetEgvsResponse getEgvs(GetEgvsRequest request) {
		String authCode = deviceUserService
				.getDeviceUser(UUID.fromString(request.getDeviceId()), manufacturerName)
				.getApiKey();

		DexcomEgvs egvs = dexcomService.getEgvs(
				authCode,
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getStartDate()), ZoneId.systemDefault()),
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getEndDate()), ZoneId.systemDefault()));
		return dexcomMapper.toEgvsGrpc(egvs);
	}

	public GetDataRangeResponse getDataRange(GetDataRangeRequest request) {
		String authCode = deviceUserService
				.getDeviceUser(UUID.fromString(request.getDeviceId()), manufacturerName)
				.getApiKey();

		DexcomDataRange dataRange = dexcomService.getDataRange(
				authCode,
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getLastSyncTime()), ZoneId.systemDefault()));
		return dexcomMapper.toDataRangeGrpc(dataRange);
	}

	public GetDevicesResponse getDevices(GetDevicesRequest request) {
		String authCode = deviceUserService
				.getDeviceUser(UUID.fromString(request.getDeviceId()), manufacturerName)
				.getApiKey();

		DexcomDevices devices = dexcomService.getDevices(authCode).getBody();
		return dexcomMapper.toDevicesGrpc(devices);
	}

	public GetEventsResponse getEvents(GetEventsRequest request) {
		String authCode = deviceUserService
				.getDeviceUser(UUID.fromString(request.getDeviceId()), manufacturerName)
				.getApiKey();

		DexcomEvents events = dexcomService.getEvents(
				authCode,
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getStartDate()), ZoneId.systemDefault()),
				OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getEndDate()), ZoneId.systemDefault()));
		return dexcomMapper.toEventsGrpc(events);
	}
}
