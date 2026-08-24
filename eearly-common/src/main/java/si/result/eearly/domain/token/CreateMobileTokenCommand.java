package si.result.eearly.domain.token;

public record CreateMobileTokenCommand (
        String token,
        String userId
) {
}
