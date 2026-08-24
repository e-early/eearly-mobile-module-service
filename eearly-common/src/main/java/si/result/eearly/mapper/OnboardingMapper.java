package si.result.eearly.mapper;

import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import si.result.eearly.domain.user.CreateUserCommand;
import si.result.eearly.domain.user.User;
import si.result.eearly.domain.user.UserToken;
import si.result.eearly.dto.CreateUserDTO;
import si.result.eearly.dto.CreateUserResponseDTO;
import si.result.eearly.dto.MobileConfigurationDTO;
import si.result.eearly.dto.UserDTO;
import si.result.eearly.service.onboarding.MobileConfig;

@Mapper
public interface OnboardingMapper {
  CreateUserCommand toCreateCommand(CreateUserDTO dto);

  UserDTO toDTO(User user);

  @Mapping(source = "onboardingUrl", target = "onboardingUrl")
  @Mapping(source = "user", target = "user")
  CreateUserResponseDTO toDTO(UserDTO user, String onboardingUrl);

  @Mapping(source = "config.baseUrl", target = "keycloakBaseUrl")
  @Mapping(source = "config.realm", target = "keycloakRealm")
  @Mapping(source = "config.clientId", target = "keycloakClientId")
  @Mapping(source = "config.apiBaseUrl", target = "apiBaseUrl")
  @Mapping(source = "userId", target = "userId")
  @Mapping(source = "token.accessToken", target = "accessToken")
  @Mapping(source = "token.refreshToken", target = "refreshToken")
  MobileConfigurationDTO toDTO(MobileConfig config, UUID userId, UserToken token);
}
