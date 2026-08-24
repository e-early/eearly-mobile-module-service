package si.result.eearly.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.api_device.Heartbeat;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.device.DeviceUserCommand;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.mapper.DeviceUserMapper;
import si.result.eearly.service.ApiDeviceServiceWrapper;
import si.result.eearly.service.UserDeviceService;
import si.result.eearly.service.UserPrincipalService;
import si.result.eearly.genproto.AddApiKeyResponse;
import si.result.eearly.genproto.AddApiKeyRequest;
import si.result.eearly.genproto.ErrorCode;
import si.result.eearly.genproto.GetApiKeysResponse;
import si.result.eearly.genproto.DeleteApiKeyRequest;
import si.result.eearly.genproto.DeleteApiKeyResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDeviceServiceGrpcFacade {

  private final UserDeviceService userDeviceService;
  private final DeviceUserMapper deviceUserMapper;
  private final UserPrincipalService userPrincipalService;
  private final ApiDeviceServiceWrapper apiDeviceServiceWrapper;

  @Transactional
  public AddApiKeyResponse addApiKey(AddApiKeyRequest request) {
    var userId = userPrincipalService.getUserPrincipal().getId();
    DeviceUserCommand deviceUserCommand = deviceUserMapper.toDeviceUserCommand(userId, request.getApiKey());
    DeviceUser deviceUser =  userDeviceService.addApiKey(deviceUserCommand);
    Heartbeat heartbeat = apiDeviceServiceWrapper.checkHeartbeat(deviceUser);
    if(heartbeat.code() != 200) {
      throw new ServiceInvalidRequestException(heartbeat.error(), ErrorCode.forNumber(1));
    }
    var builder = AddApiKeyResponse.newBuilder();
    return builder.build();

  }

  @Transactional
  public AddApiKeyResponse updateApiKey(AddApiKeyRequest request) {
    var userId = userPrincipalService.getUserPrincipal().getId();
    DeviceUserCommand deviceUserCommand = deviceUserMapper.toDeviceUserCommand(userId, request.getApiKey());
    userDeviceService.updateApiKey(deviceUserCommand);
    var builder =AddApiKeyResponse.newBuilder();
    return builder.build();

  }

  @Transactional(readOnly = true)
  public GetApiKeysResponse getApiKeys() {
    String userId = userPrincipalService.getUserPrincipal().getId();
    List<DeviceUser> deviceUserList = userDeviceService.findAllByUserId(userId);
    var getApiKeysResponseBuilder = GetApiKeysResponse.newBuilder();
    for (DeviceUser deviceUser : deviceUserList) {
      getApiKeysResponseBuilder.addDevices(deviceUserMapper.toGrpcDeviceApiKey(deviceUser));
    }
    return getApiKeysResponseBuilder.build();
  }

  @Transactional
  public DeleteApiKeyResponse deleteApiKey(DeleteApiKeyRequest request) {
    String userId = userPrincipalService.getUserPrincipal().getId();
    try {
      userDeviceService.deleteApiKey(deviceUserMapper.toDeleteApiKeyCommand(request.getDeviceId(), userId));
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(), ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
    return DeleteApiKeyResponse.newBuilder().build();
  }
}
