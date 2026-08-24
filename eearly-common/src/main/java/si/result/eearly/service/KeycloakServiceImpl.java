package si.result.eearly.service;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.UpdateUsernameCommand;
import si.result.eearly.domain.user.CreateUserCommand;
import si.result.eearly.domain.user.User;
import si.result.eearly.domain.user.UserToken;
import si.result.eearly.exception.KeycloakException;
import si.result.eearly.exception.KeycloakUserCreateException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakServiceImpl implements KeycloakService {
  private final Keycloak keycloak;

  @Value("${keycloak.realm}")
  private String keycloakRealm;

  @Value("${keycloak.auth-server-url}")
  private String keycloakServerUrl;

  @Value("${keycloak.mobile.client.id}")
  private String keycloakMobileClientId;

  @Override
  public void updateUsername(UpdateUsernameCommand command) throws ValidationException {
    try {
      final var user = keycloak.realm(keycloakRealm).users().get(command.userId());
      final var representation = keycloak.realm(keycloakRealm).users().get(command.userId()).toRepresentation();
      representation.setUsername(command.username());
      user.update(representation);
    } catch (ClientErrorException e) {
      if(e.getMessage().contains("409")) {
        throw new ValidationException(ErrorCode.ALREADY_EXISTS, "User with given username already exists.");
      }
      throw new ValidationException(ErrorCode.INTERNAL_ERROR, "Error while updating username.");
    }

  }

  @Override
  public User createUser(CreateUserCommand command) throws ValidationException {
    if(command.firstName() == null || command.firstName().isEmpty()) {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, "First name is required field");
    }
    if(command.lastName() == null || command.lastName().isEmpty()) {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, "Last name is required field");
    }
    if(command.email() == null || command.email().isEmpty()) {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, "Email is required field");
    }

    final var userRepresentation = new UserRepresentation();
    String username =
        command.lastName().toLowerCase() +
        command.firstName().toLowerCase() +
            String.format("%05d", (int)(Math.random() * 99999));
    username = StringUtils.stripAccents(username).replaceAll(" ", "");
    userRepresentation.setUsername(username);

    userRepresentation.setFirstName(command.firstName());
    userRepresentation.setLastName(command.lastName());
    userRepresentation.setEmail(command.email());
    String createdUserId;
    userRepresentation.setEnabled(true);
    log.info("Creating user in keycloak in realm {} with username: {}", keycloakRealm, username);
    try (Response createUserResponse = keycloak.realm(keycloakRealm).users().create(userRepresentation)) {
      if (createUserResponse.getStatus() >= 400) {
        String responseBody = createUserResponse.readEntity(String.class);
        log.error("Failed to create user in keycloak with status {}. Response body: {}", createUserResponse.getStatus(), responseBody);
        
        if (createUserResponse.getStatus() == 409 && responseBody != null && responseBody.contains("same email")) {
          throw new ValidationException(ErrorCode.ALREADY_EXISTS, "User with given email already exists.");
        }

        throw new KeycloakException(createUserResponse.getStatus());
      }
      createdUserId = CreatedResponseUtil.getCreatedId(createUserResponse);
    } catch (ValidationException | KeycloakException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to create user in keycloak", e);
      throw new KeycloakUserCreateException(e.getMessage());
    }
    return new User(
        createdUserId,
        command.firstName(),
        command.lastName(),
        command.email(),
        username
    );
  }

  @Override
  public UserToken getUserToken(UUID userId) throws ValidationException {
    if(userId == null) {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, "User id must be provided");
    }
    UserResource user = keycloak.realm(keycloakRealm).users().get(userId.toString());

    String password = String.valueOf(UUID.randomUUID());
    CredentialRepresentation cred = new CredentialRepresentation();
    cred.setTemporary(false);
    cred.setType(CredentialRepresentation.PASSWORD);
    cred.setValue(password);

    keycloak.realm(keycloakRealm).users().get(userId.toString()).resetPassword(cred);

    var getUserRequest = KeycloakBuilder.builder()
        .serverUrl(keycloakServerUrl)
        .realm(keycloakRealm)
        .clientId(keycloakMobileClientId)
        .scope("offline_access")
        .username(user.toRepresentation().getUsername())
        .password(password)
        .grantType(OAuth2Constants.PASSWORD);


    try(Keycloak userClient = getUserRequest.build()) {
      var response = userClient.tokenManager().getAccessToken();
      return new UserToken(
          response.getToken(),
          response.getRefreshToken()
      );
    } catch (KeycloakException e) {
      log.error("Failed to get user token", e);
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
