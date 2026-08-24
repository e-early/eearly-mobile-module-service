package si.result.eearly.service.onboarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceDetectionService {

    private static final String DEVICE_TYPE_UNKNOWN = "unknown";

    public DeviceInfo detectDevice(String userAgent) {
        if (userAgent == null) {
            log.warn("User-Agent header is null");
            return new DeviceInfo(DEVICE_TYPE_UNKNOWN, DEVICE_TYPE_UNKNOWN);
        }

        userAgent = userAgent.toLowerCase();
        log.debug("Detecting device from User-Agent: {}", userAgent);

        if (userAgent.contains("applewebkit")) {
            return new DeviceInfo("iOS", "iPhone/iPad");
        } else if (userAgent.contains("android")) {
            if (userAgent.contains("mobile")) {
                return new DeviceInfo("Android", "Mobile");
            } else {
                return new DeviceInfo("Android", "Tablet");
            }
        }

        log.info("Could not determine device type from User-Agent: {}", userAgent);
        return new DeviceInfo(DEVICE_TYPE_UNKNOWN, DEVICE_TYPE_UNKNOWN);
    }
}
