package si.result.eearly.mapper;

import java.time.OffsetDateTime;

import org.mapstruct.Mapper;

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
import si.result.eearly.genproto.Alert;
import si.result.eearly.genproto.AlertSchedule;
import si.result.eearly.genproto.AlertScheduleSettings;
import si.result.eearly.genproto.AlertScheduleSettingsOverride;
import si.result.eearly.genproto.AlertSettings;
import si.result.eearly.genproto.Calibration;
import si.result.eearly.genproto.DataRange;
import si.result.eearly.genproto.Device;
import si.result.eearly.genproto.Egv;
import si.result.eearly.genproto.Event;
import si.result.eearly.genproto.GetAlertsResponse;
import si.result.eearly.genproto.GetCalibrationsResponse;
import si.result.eearly.genproto.GetDataRangeResponse;
import si.result.eearly.genproto.GetDevicesResponse;
import si.result.eearly.genproto.GetEgvsResponse;
import si.result.eearly.genproto.GetEventsResponse;
import si.result.eearly.genproto.Range;

@Mapper
public interface DexcomMapper {

	DexcomAlertsDTO toAlertsDTO(DexcomAlerts alerts);

	DexcomCalibrationsDTO toCalibrationsDTO(DexcomCalibrations calibrations);

	DexcomEgvsDTO toEgvsDTO(DexcomEgvs egvs);

	DexcomDataRangeDTO toDataRangeDTO(DexcomDataRange dataRange);

	DexcomDevicesDTO toDevicesDTO(DexcomDevices devices);

	DexcomEventsDTO toEventsDTO(DexcomEvents events);

	default GetAlertsResponse toAlertsGrpc(DexcomAlerts alerts) {

		GetAlertsResponse.Builder builder = GetAlertsResponse.newBuilder();
		builder
				.setRecordType(alerts.getRecordType())
				.setRecordVersion(alerts.getRecordVersion())
				.setUserId(alerts.getUserId());
		for (DexcomAlerts.Alert alert : alerts.getRecords()) {
			builder.addRecords(Alert.newBuilder()
					.setRecordId(alert.getRecordId())
					.setSystemTime(alert.getSystemTime().toString())
					.setDisplayTime(alert.getDisplayTime().toString())
					.setAlertName(alert.getAlertName())
					.setAlertState(alert.getAlertState())
					.setDisplayDevice(alert.getDisplayDevice())
					.setTransmitterGeneration(alert.getTransmitterGeneration())
					.setTransmitterId(alert.getTransmitterId())
					.setDisplayApp(alert.getDisplayApp())
					.build());
		}
		return builder.build();
	}

	default GetCalibrationsResponse toCalibrationsGrpc(DexcomCalibrations calibrations) {

		GetCalibrationsResponse.Builder builder = GetCalibrationsResponse.newBuilder();
		builder
				.setRecordType(calibrations.getRecordType())
				.setRecordVersion(calibrations.getRecordVersion())
				.setUserId(calibrations.getUserId());
		for (DexcomCalibrations.Calibration calibration : calibrations.getRecords()) {
			builder.addRecords(Calibration.newBuilder()
					.setRecordId(calibration.getRecordId())
					.setSystemTime(calibration.getSystemTime().toString())
					.setDisplayTime(calibration.getDisplayTime().toString())
					.setUnit(calibration.getUnit())
					.setValue(calibration.getValue())
					.setDisplayDevice(calibration.getDisplayDevice())
					.setTransmitterId(calibration.getTransmitterId())
					.setTransmitterTicks(calibration.getTransmitterTicks())
					.setTransmitterGeneration(calibration.getTransmitterGeneration())
					.build());
		}
		return builder.build();
	}

	default GetDataRangeResponse toDataRangeGrpc(DexcomDataRange dataRange) {

		GetDataRangeResponse.Builder builder = GetDataRangeResponse.newBuilder();
		builder
				.setRecordType(dataRange.getRecordType())
				.setRecordVersion(dataRange.getRecordVersion())
				.setUserId(dataRange.getUserId());

		if (dataRange.getCalibrations() != null) {
			builder.setCalibrations(DataRange.newBuilder()
					.setStart(Range.newBuilder()
							.setSystemTime(dataRange.getCalibrations().getStart().getSystemTime().toString())
							.setDisplayTime(dataRange.getCalibrations().getStart().getDisplayTime().toString()))
					.setEnd(Range.newBuilder()
							.setSystemTime(dataRange.getCalibrations().getEnd().getSystemTime().toString())
							.setDisplayTime(dataRange.getCalibrations().getEnd().getDisplayTime().toString()))
					.build());
		}
		if (dataRange.getEgvs() != null) {
			builder.setEgvs(DataRange.newBuilder()
					.setStart(Range.newBuilder()
							.setSystemTime(dataRange.getEgvs().getStart().getSystemTime().toString())
							.setDisplayTime(dataRange.getEgvs().getStart().getDisplayTime().toString()))
					.setEnd(Range.newBuilder()
							.setSystemTime(dataRange.getEgvs().getEnd().getSystemTime().toString())
							.setDisplayTime(dataRange.getEgvs().getEnd().getDisplayTime().toString()))
					.build());
		}
		if (dataRange.getEvents() != null) {
			builder.setEvents(DataRange.newBuilder()
					.setStart(Range.newBuilder()
							.setSystemTime(dataRange.getEvents().getStart().getSystemTime().toString())
							.setDisplayTime(dataRange.getEvents().getStart().getDisplayTime().toString()))
					.setEnd(Range.newBuilder()
							.setSystemTime(dataRange.getEvents().getEnd().getSystemTime().toString())
							.setDisplayTime(dataRange.getEvents().getEnd().getDisplayTime().toString()))
					.build());
		}
		return builder.build();
	}

	default GetDevicesResponse toDevicesGrpc(DexcomDevices devices) {

		GetDevicesResponse.Builder builder = GetDevicesResponse.newBuilder();
		builder
				.setRecordType(devices.getRecordType())
				.setRecordVersion(devices.getRecordVersion())
				.setUserId(devices.getUserId());
		for (DexcomDevices.Device device : devices.getRecords()) {
			Device.Builder deviceBuilder = Device.newBuilder()
					.setTransmitterGeneration(device.getTransmitterGeneration())
					.setDisplayDevice(device.getDisplayDevice())
					.setDisplayApp(device.getDisplayApp())
					.setLastUploadDate(device.getLastUploadDate().toString())
					.setTransmitterId(device.getTransmitterId());
			for (DexcomDevices.AlertSchedule alertSchedule : device.getAlertSchedules()) {
				AlertSchedule.Builder alertScheduleBuilder = AlertSchedule
						.newBuilder()
						.setAlertScheduleSettings(AlertScheduleSettings.newBuilder()
								.setAlertScheduleName(alertSchedule.getAlertScheduleSettings().getAlertScheduleName())
								.setIsEnabled(alertSchedule.getAlertScheduleSettings().getIsEnabled())
								.setStartTime(alertSchedule.getAlertScheduleSettings().getStartTime().toString())
								.setEndTime(alertSchedule.getAlertScheduleSettings().getEndTime().toString())
								.setIsActive(alertSchedule.getAlertScheduleSettings().getIsActive())
								.setOverride(AlertScheduleSettingsOverride
										.newBuilder()
										.setIsOverrideEnabled(alertSchedule.getAlertScheduleSettings().getOverride().getIsOverrideEnabled())
										.setMode(alertSchedule.getAlertScheduleSettings().getOverride().getMode())
										.setEndTime(alertSchedule.getAlertScheduleSettings().getOverride().getEndTime().toString())
										.build())
								.addAllDaysOfWeek(alertSchedule.getAlertScheduleSettings().getDaysOfWeek()));
				for (DexcomDevices.AlertSettings alertSettings : alertSchedule.getAlertSettings()) {
					AlertSettings.Builder alertSettingsBuilder = AlertSettings
							.newBuilder()
							.setSystemTime(alertSettings.getSystemTime().toString())
							.setDisplayTime(alertSettings.getDisplayTime().toString())
							.setAlertName(alertSettings.getAlertName())
							.setValue(alertSettings.getValue())
							.setUnit(alertSettings.getUnit())
							.setEnabled(alertSettings.getEnabled())
							.setSoundTheme(alertSettings.getSoundTheme())
							.setSoundOutputMode(alertSettings.getSoundOutputMode());

					if (alertSettings.getSnooze() != null) {
						alertSettingsBuilder.setSnooze(alertSettings.getSnooze());
					}
					if (alertSettings.getSecondaryTriggerCondition() != null) {
						alertSettingsBuilder.setSecondaryTriggerCondition(alertSettings.getSecondaryTriggerCondition());
					}

					alertScheduleBuilder.addAlertSettings(alertSettingsBuilder.build());
				}
				deviceBuilder.addAlertSchedules(alertScheduleBuilder.build());
			}
			builder.addRecords(deviceBuilder.build());
		}
		return builder.build();
	}

	default GetEgvsResponse toEgvsGrpc(DexcomEgvs egvs) {

		GetEgvsResponse.Builder builder = GetEgvsResponse.newBuilder();
		builder
				.setRecordType(egvs.getRecordType())
				.setRecordVersion(egvs.getRecordVersion())
				.setUserId(egvs.getUserId());
		for (DexcomEgvs.Egv egv : egvs.getRecords()) {
			builder.addRecords(Egv.newBuilder()
					.setRecordId(egv.getRecordId())
					.setSystemTime(egv.getSystemTime().toString())
					.setDisplayTime(egv.getDisplayTime().toString())
					.setTransmitterId(egv.getTransmitterId())
					.setTransmitterTicks(egv.getTransmitterTicks())
					.setValue(egv.getValue())
					.setTrend(egv.getTrend())
					.setTrendRate(egv.getTrendRate())
					.setUnit(egv.getUnit())
					.setRateUnit(egv.getRateUnit())
					.setDisplayDevice(egv.getDisplayDevice())
					.setTransmitterGeneration(egv.getTransmitterGeneration())
					.build());
		}
		return builder.build();

	}

	default GetEventsResponse toEventsGrpc(DexcomEvents events) {

		GetEventsResponse.Builder builder = GetEventsResponse.newBuilder();
		builder
				.setRecordType(events.getRecordType())
				.setRecordVersion(events.getRecordVersion())
				.setUserId(events.getUserId());
		for (DexcomEvents.Event event : events.getRecords()) {
			Event.Builder eventBuilder = Event.newBuilder()
					.setRecordId(event.getRecordId())
					.setSystemTime(event.getSystemTime().toString())
					.setDisplayTime(event.getDisplayTime().toString())
					.setEventStatus(event.getEventStatus())
					.setEventType(event.getEventType())
					.setValue(event.getValue())
					.setUnit(event.getUnit())
					.setTransmitterId(event.getTransmitterId())
					.setTransmitterGeneration(event.getTransmitterGeneration())
					.setDisplayDevice(event.getDisplayDevice());

			if (event.getEventSubType() != null) {
				eventBuilder.setEventSubType(event.getEventSubType());
			}

			builder.addRecords(eventBuilder.build());
		}
		return builder.build();
	}

	default String map(OffsetDateTime value) {
		return value.toString();
	}
}
