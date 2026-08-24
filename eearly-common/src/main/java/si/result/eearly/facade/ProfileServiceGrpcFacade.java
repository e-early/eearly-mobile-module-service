package si.result.eearly.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.exception.AlreadyExistsException;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.genproto.SendFCMNotificationForTokenResponse;
import si.result.eearly.genproto.SendFCMNotificationForTokenRequest;
import si.result.eearly.genproto.ErrorCode;
import si.result.eearly.genproto.UpsertProfileRequest;
import si.result.eearly.genproto.UpsertProfileResponse;
import si.result.eearly.mapper.ProfileMapper;
import si.result.eearly.service.KeycloakService;
import si.result.eearly.service.ProfileService;
import si.result.eearly.service.UserPrincipalService;
import si.result.eearly.genproto.UpdateUsernameRequest;
import si.result.eearly.genproto.UpdateUsernameResponse;
import si.result.eearly.genproto.GetProfileResponse;

@Service
@RequiredArgsConstructor
public class ProfileServiceGrpcFacade {

  private final ProfileService profileService;
  private final UserPrincipalService userPrincipalService;
  private final ProfileMapper profileMapper;
  private final KeycloakService keycloakService;

  public UpsertProfileResponse upsertProfile(UpsertProfileRequest request) {

    var userId = userPrincipalService.getUserPrincipal().getId();

    try {
      return profileMapper.toUpsertProfileResponse(
          profileService.upsertProfile(
              profileMapper.toUpsertCommand(request, userId)));
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(), ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
  }

  public SendFCMNotificationForTokenResponse sendNotificationForToken(SendFCMNotificationForTokenRequest request) {
   String returnMessage = profileService.sendNotificationForToken(
        request.getToken(),
        request.getTitle(),
        request.getBody());

    return SendFCMNotificationForTokenResponse.newBuilder()
        .setFcmReturnMessage(returnMessage)
        .build();
  }

  public UpdateUsernameResponse updateUsername(UpdateUsernameRequest request) {
    var userId = userPrincipalService.getUserPrincipal().getId();
    try {
      keycloakService.updateUsername(profileMapper.toUpdateUsernameCommand(userId, request.getUsername()));
    } catch (ValidationException e) {
      throw new AlreadyExistsException(e.getMessage(), ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }

    return UpdateUsernameResponse.newBuilder().build();
  }

  public GetProfileResponse getProfile() {
    GetProfileResponse.Builder response = GetProfileResponse.newBuilder();
    var userId = userPrincipalService.getUserPrincipal().getId();
    var username = userPrincipalService.getUserPrincipal().getUsername();
    try {
      Profile profile = profileService.getProfile(userId);
      response.setProfile(profileMapper.toGrpcProfile(profile, username));

    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(), ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
    return response.build();
  }
}
