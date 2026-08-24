package si.result.eearly.service;

import si.result.eearly.domain.device.Device;

import java.util.List;
import java.util.Optional;

public interface DeviceService {
    Optional<Device> getSupportedDevice(String deviceId);

    List<Device> getAllDevicesForManufacturer(String manufacturerId);

}

