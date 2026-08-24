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
public class DexcomEvents {

	@JsonProperty("recordType")
	private String recordType;
	@JsonProperty("recordVersion")
	private String recordVersion;
	@JsonProperty("userId")
	private String userId;
	@JsonProperty("records")
	private List<Event> records;

	@Data
	public static class Event {

		@JsonProperty("recordId")
		private String recordId;
		@JsonProperty("systemTime")
		private OffsetDateTime systemTime;
		@JsonProperty("displayTime")
		private OffsetDateTime displayTime;
		@JsonProperty("eventStatus")
		private String eventStatus;
		@JsonProperty("eventType")
		private String eventType;
		@JsonProperty("eventSubType")
		private String eventSubType;
		@JsonProperty("value")
		private Double value;
		@JsonProperty("unit")
		private String unit;
		@JsonProperty("transmitterId")
		private String transmitterId;
		@JsonProperty("transmitterGeneration")
		private String transmitterGeneration;
		@JsonProperty("displayDevice")
		private String displayDevice;
	}
}
