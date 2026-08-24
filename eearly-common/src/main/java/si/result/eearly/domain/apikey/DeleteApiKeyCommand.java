package si.result.eearly.domain.apikey;

public record DeleteApiKeyCommand(
        String deviceId,
        String userId
) {
}
