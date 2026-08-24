package si.result.eearly.domain.apikey;

public record ApiKeyCommand(

    String userId,
    String deviceId,
    String value
) {
}
