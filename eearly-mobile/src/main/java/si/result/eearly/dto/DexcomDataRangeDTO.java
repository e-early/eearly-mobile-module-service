package si.result.eearly.dto;

public record DexcomDataRangeDTO(
        String recordType,
        String recordVersion,
        String userId,
        DataRangeDTO calibrations,
        DataRangeDTO egvs,
        DataRangeDTO events) {

    public record DataRangeDTO(
            RangeDTO start,
            RangeDTO end) {
    }

    public record RangeDTO(
            String systemTime,
            String displayTime) {
    }
}
