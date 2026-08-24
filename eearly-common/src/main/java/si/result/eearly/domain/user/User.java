package si.result.eearly.domain.user;

public record User(
    String id,
    String firstName,
    String lastName,
    String email,
    String userName
) {
}
