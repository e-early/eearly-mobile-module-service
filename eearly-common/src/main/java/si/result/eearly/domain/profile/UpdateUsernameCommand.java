package si.result.eearly.domain.profile;

public record UpdateUsernameCommand(
        String userId,
        String username
) {
}
