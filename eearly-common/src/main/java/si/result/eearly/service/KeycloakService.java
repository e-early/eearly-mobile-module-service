package si.result.eearly.service;

import java.util.UUID;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.UpdateUsernameCommand;
import si.result.eearly.domain.user.CreateUserCommand;
import si.result.eearly.domain.user.User;
import si.result.eearly.domain.user.UserToken;

public interface KeycloakService {
  void updateUsername(UpdateUsernameCommand command) throws ValidationException;

  User createUser(CreateUserCommand command) throws ValidationException;

  UserToken getUserToken(UUID userId) throws ValidationException;
}
