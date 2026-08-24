package si.result.eearly.service;

import si.result.eearly.domain.device.DeviceUser;

import java.util.UUID;

public interface DeviceUserService {
	DeviceUser getDeviceUser(UUID deviceId, String manufacturerName);
}
