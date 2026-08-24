package si.result.eearly.dto;

public record CreateUserResponseDTO(
    UserDTO user,
    String onboardingUrl
) {

}
