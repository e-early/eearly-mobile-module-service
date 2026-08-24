package si.result.eearly.dto;

import java.util.List;

public record DexcomAlertsDTO(
    String recordType,
    String recordVersion,
    String userId,
    List<AlertDTO> records) {
    
  public record AlertDTO(
      String recordId,
      String systemTime,
      String displayTime,
      String alertName,
      String alertState,
      String displayDevice,
      String transmitterGeneration,
      String transmitterId,
      String displayApp) {
  }
}
