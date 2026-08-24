package si.result.eearly.domain.user;

public record UserToken(
    String accessToken,
    String refreshToken
) {

}
