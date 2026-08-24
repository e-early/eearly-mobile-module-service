package si.result.eearly.dto;

import java.util.List;

public record DexcomDevicesDTO(
		String recordType,
		String recordVersion,
		String userId,
		List<DeviceDTO> records) {

	public record DeviceDTO(
			String transmitterGeneration,
			String displayDevice,
			String displayApp,
			String lastUploadDate,
			String transmitterId,
			List<AlertScheduleDTO> alertSchedules) {
	}

	public record AlertScheduleDTO(
			AlertScheduleSettingsDTO alertScheduleSettings,
			List<AlertSettingsDTO> alertSettings) {
	}

	public record AlertScheduleSettingsDTO(
			String alertScheduleName,
			Boolean isEnabled,
			String startTime,
			String endTime,
			Boolean isActive,
			AlertScheduleSettingsOverrideDTO override,
			List<String> daysOfWeek) {
	}

	public record AlertScheduleSettingsOverrideDTO(
			Boolean isOverrideEnabled,
			String mode,
			String endTime) {
	}

	public record AlertSettingsDTO(
			String systemTime,
			String displayTime,
			String alertName,
			Integer value,
			String unit,
			Integer snooze,
			Boolean enabled,
			Integer secondaryTriggerCondition,
			String soundTheme,
			String soundOutputMode) {
	}
}
