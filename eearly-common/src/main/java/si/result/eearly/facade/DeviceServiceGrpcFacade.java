package si.result.eearly.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.device.Device;
import si.result.eearly.domain.exception.NotFoundException;
import si.result.eearly.mapper.DeviceMapper;
import si.result.eearly.service.DeviceService;
import si.result.eearly.genproto.GetSupportedDeviceResponse;
import si.result.eearly.genproto.GetSupportedDevicesForManufacturerResponse;


@Service
@RequiredArgsConstructor
public class DeviceServiceGrpcFacade {

    private final DeviceService deviceService;
    private final DeviceMapper deviceMapper;

    @Transactional(readOnly = true)
    public GetSupportedDeviceResponse getSupportedDevice(String deviceId) {
        final Device device = deviceService.getSupportedDevice(deviceId).orElseThrow(
                () -> new NotFoundException("No device found with id: " + deviceId));

        GetSupportedDeviceResponse resp = deviceMapper.deviceToSupportedDeviceResponse(device);

        return resp;
    }

    @Transactional(readOnly = true)
    public GetSupportedDevicesForManufacturerResponse getSupportedDevicesForManufacturer(String manufacturerId) {
        final var devices = deviceService.getAllDevicesForManufacturer(manufacturerId);

        return deviceMapper.toSupportedDevicesForManufacturer(devices);
    }
}
