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
public class DexcomAlerts {

	@JsonProperty("recordType")
	private String recordType;
	@JsonProperty("recordVersion")
	private String recordVersion;
	@JsonProperty("userId")
	private String userId;
	@JsonProperty("records")
	private List<Alert> records;

	@Data
	public static class Alert {
		
		@JsonProperty("recordId")
		private String recordId;
		@JsonProperty("systemTime")
		private OffsetDateTime systemTime;
		@JsonProperty("displayTime")
		private OffsetDateTime displayTime;
		@JsonProperty("alertName")
		private String alertName;
		@JsonProperty("alertState")
		private String alertState;
		@JsonProperty("displayDevice")
		private String displayDevice;
		@JsonProperty("transmitterGeneration")
		private String transmitterGeneration;
		@JsonProperty("transmitterId")
		private String transmitterId;
		@JsonProperty("displayApp")
		private String displayApp;
	}
}
