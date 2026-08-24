package si.result.eearly.service;

import si.result.eearly.domain.apikey.DeleteApiKeyCommand;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.device.DeviceUserCommand;
import si.result.eearly.domain.exception.ValidationException;

import java.util.List;

public interface UserDeviceService {

  DeviceUser addApiKey(DeviceUserCommand deviceUserCommand);
  void updateApiKey(DeviceUserCommand deviceUserCommand);
  DeviceUser findById(String deviceId, String userId) throws ValidationException;
  List<DeviceUser> findAllByUserId(String userId);
  void deleteApiKey(DeleteApiKeyCommand deleteApiKeyCommand) throws ValidationException;
}
