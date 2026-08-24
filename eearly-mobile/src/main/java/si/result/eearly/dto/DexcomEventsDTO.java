package si.result.eearly.dto;

import java.util.List;

public record DexcomEventsDTO(
		String recordType,
		String recordVersion,
		String userId,
		List<EventDTO> records) {

	public record EventDTO(
			String recordId,
			String systemTime,
			String displayTime,
			String eventStatus,
			String eventType,
			String eventSubType,
			Double value,
			String unit,
			String transmitterId,
			String transmitterGeneration,
			String displayDevice) {
	}
}
