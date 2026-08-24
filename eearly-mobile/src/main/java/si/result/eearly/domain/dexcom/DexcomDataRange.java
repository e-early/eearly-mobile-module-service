package si.result.eearly.domain.dexcom;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DexcomDataRange {

	@JsonProperty("recordType")
	private String recordType;
	@JsonProperty("recordVersion")
	private String recordVersion;
	@JsonProperty("userId")
	private String userId;
	@JsonProperty("calibrations")
	private DataRange calibrations;
	@JsonProperty("egvs")
	private DataRange egvs;
	@JsonProperty("events")
	private DataRange events;

	@Data
	public static class DataRange {

		@JsonProperty("start")
		private Range start;
		@JsonProperty("end")
		private Range end;
	}

	@Data
	public static class Range {
		
		@JsonProperty("systemTime")
		private LocalDateTime systemTime;
		@JsonProperty("displayTime")
		private LocalDateTime displayTime;
	}
}
