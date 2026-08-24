package si.result.eearly.domain.user;

public record CreateUserCommand(
    String firstName,
    String lastName,
    String email
) {
}
