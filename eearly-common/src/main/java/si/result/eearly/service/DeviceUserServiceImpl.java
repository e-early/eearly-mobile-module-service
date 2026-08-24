package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import si.result.eearly.domain.UserPrincipal;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.repository.DeviceUserRepository;

import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeviceUserServiceImpl implements DeviceUserService {

	private final UserPrincipalService userService;
	private final DeviceUserRepository deviceUserRepository;

	@Override
	public DeviceUser getDeviceUser(UUID deviceId, String manufacturerName) {
		UserPrincipal user = userService.getUserPrincipal();

		DeviceUser deviceUser = deviceUserRepository
				.findByUserAndDevice(UUID.fromString(user.getId()), deviceId)
				.orElseThrow(() -> new IllegalArgumentException("Device not found"));

		log.info("Device found: {}", deviceUser.getDevice().getManufacturer().getName());

		if (!deviceUser.getDevice().getManufacturer().getName().equals(manufacturerName)) {
			log.warn("Found device manufacturer '{}' does not match '{}'",
					deviceUser.getDevice().getManufacturer().getName(),
					manufacturerName);
			throw new IllegalArgumentException("Device is not " + manufacturerName);
		}

		return deviceUser;
	}

}
