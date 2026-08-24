package si.result.eearly.domain.device;

public record DeviceUserCommand (
    String userId,
    String deviceId,
    String apiKey
){
}
