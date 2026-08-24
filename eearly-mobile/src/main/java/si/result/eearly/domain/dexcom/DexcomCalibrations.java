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
public class DexcomCalibrations {

	@JsonProperty("recordType")
	private String recordType;
	@JsonProperty("recordVersion")
	private String recordVersion;
	@JsonProperty("userId")
	private String userId;
	@JsonProperty("records")
	private List<Calibration> records;

	@Data
	public static class Calibration {

		@JsonProperty("recordId")
		private String recordId;
		@JsonProperty("systemTime")
		private OffsetDateTime systemTime;
		@JsonProperty("displayTime")
		private OffsetDateTime displayTime;
		@JsonProperty("unit")
		private String unit;
		@JsonProperty("value")
		private Integer value;
		@JsonProperty("displayDevice")
		private String displayDevice;
		@JsonProperty("transmitterId")
		private String transmitterId;
		@JsonProperty("transmitterTicks")
		private Integer transmitterTicks;
		@JsonProperty("transmitterGeneration")
		private String transmitterGeneration;
	}
}
