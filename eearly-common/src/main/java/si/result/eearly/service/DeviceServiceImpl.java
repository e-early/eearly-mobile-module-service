package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.device.Device;
import si.result.eearly.repository.DeviceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    @Override
    public Optional<Device> getSupportedDevice(String deviceId) {
        return deviceRepository.findById(UUID.fromString(deviceId));
    }

    @Override
    public List<Device> getAllDevicesForManufacturer(String manufacturerId) {
        return deviceRepository.findAllByManufacturerId(UUID.fromString(manufacturerId));
    }
}
