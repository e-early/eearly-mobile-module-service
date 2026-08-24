package si.result.eearly.domain.token;

public record UpdateMobileTokenCommand (
        String oldToken,
        String newToken,
        String userId
) {
}
