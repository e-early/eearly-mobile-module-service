package si.result.eearly.domain.dexcom;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.enums.MeasurementTypeEnum;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementType;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DexcomEgvs {

	@JsonProperty("recordType")
	private String recordType;
	@JsonProperty("recordVersion")
	private String recordVersion;
	@JsonProperty("userId")
	private String userId;
	@JsonProperty("records")
	private List<Egv> records;

	@Data
	public static class Egv {

		@JsonProperty("recordId")
		private String recordId;
		@JsonProperty("systemTime")
		private OffsetDateTime systemTime;
		@JsonProperty("displayTime")
		private OffsetDateTime displayTime;
		@JsonProperty("transmitterId")
		private String transmitterId;
		@JsonProperty("transmitterTicks")
		private Integer transmitterTicks;
		@JsonProperty("value")
		private Integer value;
		@JsonProperty("trend")
		private String trend;
		@JsonProperty("trendRate")
		private Integer trendRate;
		@JsonProperty("unit")
		private String unit;
		@JsonProperty("rateUnit")
		private String rateUnit;
		@JsonProperty("displayDevice")
		private String displayDevice;
		@JsonProperty("transmitterGeneration")
		private String transmitterGeneration;

		public Measurement toMeasurement(DeviceUser deviceUser, MeasurementType measurementType) {
			if (measurementType.getMeasurementType() != MeasurementTypeEnum.BLOOD_GLUCOSE) {
				throw new IllegalArgumentException("Measurement type must be blood glucose");
			}

			OffsetDateTime measuredAt = systemTime != null ? systemTime : displayTime;
			if (measuredAt == null) {
				measuredAt = OffsetDateTime.now();
			}

      Measurement measurement = new Measurement(
          deviceUser.getProfile().getUserId(),
          measurementType.getObservationId(),
          measurementType.getId(),
          measurementType.getUnit(),
          getValue().doubleValue(),
          UUID.randomUUID(),
          measuredAt.toZonedDateTime(),
          deviceUser.getDevice().getId()
      );
			return measurement;
		}
	}
}
