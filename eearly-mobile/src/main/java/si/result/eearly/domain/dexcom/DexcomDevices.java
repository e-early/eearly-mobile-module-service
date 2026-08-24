package si.result.eearly.domain.dexcom;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DexcomDevices {

	@JsonProperty("recordType")
	private String recordType;
	@JsonProperty("recordVersion")
	private String recordVersion;
	@JsonProperty("userId")
	private String userId;
	@JsonProperty("records")
	private List<Device> records;

	@Data
	public static class Device {

		@JsonProperty("transmitterGeneration")
		private String transmitterGeneration;
		@JsonProperty("displayDevice")
		private String displayDevice;
		@JsonProperty("displayApp")
		private String displayApp;
		@JsonProperty("lastUploadDate")
		private OffsetDateTime lastUploadDate;
		@JsonProperty("transmitterId")
		private String transmitterId;
		@JsonProperty("alertSchedules")
		private List<AlertSchedule> alertSchedules;
	}

	@Data
	public static class AlertSchedule {

		@JsonProperty("alertScheduleSettings")
		private AlertScheduleSettings alertScheduleSettings;
		@JsonProperty("alertSettings")
		private List<AlertSettings> alertSettings;
	}

	@Data
	public static class AlertScheduleSettings {

		@JsonProperty("alertScheduleName")
		private String alertScheduleName;
		@JsonProperty("isEnabled")
		private Boolean isEnabled;
		@JsonProperty("startTime")
		private String startTime;
		@JsonProperty("endTime")
		private String endTime;
		@JsonProperty("isActive")
		private Boolean isActive;
		@JsonProperty("override")
		private AlertScheduleSettingsOverride override;
		@JsonProperty("daysOfWeek")
		private List<String> daysOfWeek;
	}

	@Data
	public static class AlertScheduleSettingsOverride {

		@JsonProperty("isOverrideEnabled")
		private Boolean isOverrideEnabled;
		@JsonProperty("mode")
		private String mode;
		@JsonProperty("endTime")
		private String endTime;
	}

	@Data
	public static class AlertSettings {

		@JsonProperty("systemTime")
		private OffsetDateTime systemTime;
		@JsonProperty("displayTime")
		private OffsetDateTime displayTime;
		@JsonProperty("alertName")
		private String alertName;
		@JsonProperty("value")
		private Integer value;
		@JsonProperty("unit")
		private String unit;
		@JsonProperty("snooze")
		private Integer snooze;
		@JsonProperty("enabled")
		private Boolean enabled;
		@JsonProperty("SecondaryTriggerCondition")
		private Integer secondaryTriggerCondition;
		@JsonProperty("soundTheme")
		private String soundTheme;
		@JsonProperty("soundOutputMode")
		private String soundOutputMode;
	}
}
