package si.result.eearly.dto;

import java.util.List;

public record DexcomCalibrationsDTO(
        String recordType,
        String recordVersion,
        String userId,
        List<CalibrationDTO> records) {

    public record CalibrationDTO(
            String recordId,
            String systemTime,
            String displayTime,
            String unit,
            Integer value,
            String displayDevice,
            String transmitterId,
            Integer transmitterTicks,
            String transmitterGeneration) {
    }
}
