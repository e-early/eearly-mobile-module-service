package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.apikey.DeleteApiKeyCommand;
import si.result.eearly.domain.device.Device;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.device.DeviceUserCommand;
import si.result.eearly.domain.device.DeviceUserId;
import si.result.eearly.domain.exception.DeviceUserAlreadyExistsException;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.repository.DeviceRepository;
import si.result.eearly.domain.exception.NotFoundException;
import si.result.eearly.repository.DeviceUserRepository;
import si.result.eearly.repository.ProfileRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeviceServiceImpl implements UserDeviceService {

  private final DeviceUserRepository deviceUserRepository;
  private final DeviceRepository deviceRepository;
  private final ProfileRepository profileRepository;

  @Override
  public DeviceUser addApiKey(DeviceUserCommand deviceUserCommand) {

    Device device = deviceRepository.findById(UUID.fromString(deviceUserCommand.deviceId())).orElseThrow(
            () -> new NotFoundException(String.format("Device with ID %s does not exist", deviceUserCommand.deviceId())));
    Profile profile  = profileRepository.findById(UUID.fromString(deviceUserCommand.userId())).orElseThrow(
            () -> new NotFoundException(String.format("User with ID %s does not exist", deviceUserCommand.userId())));
    DeviceUser deviceUser = DeviceUser.create(deviceUserCommand.apiKey(), device, profile);
    // Check if DeviceUser with this ID already exists
    if (deviceUserRepository.existsById(deviceUser.getPrimaryKey())) {
      throw new DeviceUserAlreadyExistsException("ApiKey for this Device and User already exists.");
    }
    return deviceUserRepository.save(deviceUser);
  }

  @Override
  public void updateApiKey(DeviceUserCommand deviceUserCommand) {
    Device device = deviceRepository.findById(UUID.fromString(deviceUserCommand.deviceId())).orElseThrow(
            () -> new NotFoundException(String.format("Device with ID %s does not exist", deviceUserCommand.deviceId())));
    Profile profile  = profileRepository.findById(UUID.fromString(deviceUserCommand.userId())).orElseThrow(
            () -> new NotFoundException(String.format("Device with ID %s does not exist", deviceUserCommand.deviceId())));

    DeviceUser deviceUser = DeviceUser.create(deviceUserCommand.apiKey(), device, profile);
    // Check for existing DeviceUser record
    deviceUserRepository.findById(deviceUser.getPrimaryKey())
        .orElseThrow(() -> new NotFoundException("DeviceUser with this ID does not exist"));

    deviceUserRepository.save(deviceUser);
  }

  @Override
  public DeviceUser findById(String deviceId, String userId) throws ValidationException {
    DeviceUserId deviceUserId;
    try {
      deviceUserId = new DeviceUserId(
              deviceRepository.getReferenceById(
                      UUID.fromString(deviceId)),
              profileRepository.getReferenceById(
                      UUID.fromString(userId)
              ));

    } catch (IllegalArgumentException e) {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, "Device or user id is not a valid UUID");
    }
    return deviceUserRepository.findById(deviceUserId).orElseThrow(
            () -> new NotFoundException("DeviceUser with this ID does not exist"));
  }

  @Override
  public List<DeviceUser> findAllByUserId(String userId) {
    return deviceUserRepository.findAllByPrimaryKeyProfileUserId(UUID.fromString(userId));
  }

  @Override
  public void deleteApiKey(DeleteApiKeyCommand deleteApiKeyCommand) throws ValidationException {
    DeviceUserId deviceUserId;
    try {
      deviceUserId = new DeviceUserId(
              deviceRepository.getReferenceById(
                      UUID.fromString(deleteApiKeyCommand.deviceId())),
              profileRepository.getReferenceById(
                      UUID.fromString(deleteApiKeyCommand.userId())
              ));

      deviceUserRepository.findById(deviceUserId).ifPresent(deviceUserRepository::delete);

    } catch (IllegalArgumentException e) {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, "Device or user id is not a valid UUID");
    }
  }
}
