package si.result.eearly.dto;

import java.util.List;

public record DexcomEgvsDTO(
		String recordType,
		String recordVersion,
		String userId,
		List<EgvDTO> records) {

	public record EgvDTO(
			String recordId,
			String systemTime,
			String displayTime,
			String transmitterId,
			Integer transmitterTicks,
			Integer value,
			String trend,
			Integer trendRate,
			String unit,
			String rateUnit,
			String displayDevice,
			String transmitterGeneration) {
	}
}
