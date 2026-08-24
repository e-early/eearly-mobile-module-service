package si.result.eearly.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import si.result.eearly.domain.apikey.DeleteApiKeyCommand;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.device.DeviceUserCommand;
import si.result.eearly.genproto.ApiKey;
import si.result.eearly.genproto.DeviceApiKey;
@Mapper
public interface DeviceUserMapper {

  @Mapping(source = "userId", target = "userId")
  @Mapping(source = "apiKey.deviceId", target = "deviceId")
  @Mapping(source = "apiKey.value", target = "apiKey")
  DeviceUserCommand toDeviceUserCommand(String userId, ApiKey apiKey);

  @Mapping(source = "primaryKey.device.id", target = "deviceId")
  @Mapping(source = "primaryKey.device.name", target = "name")
  @Mapping(source = "apiKey", target = "apiKey")
  DeviceApiKey toGrpcDeviceApiKey(DeviceUser deviceUser);

  @Mapping(source = "deviceId", target = "deviceId")
  @Mapping(source = "userId", target = "userId")
  DeleteApiKeyCommand toDeleteApiKeyCommand(String deviceId, String userId);
}



