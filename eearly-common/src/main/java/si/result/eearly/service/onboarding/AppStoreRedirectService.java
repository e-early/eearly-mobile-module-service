package si.result.eearly.service.onboarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppStoreRedirectService {

    @Value("${onboarding.app.ios.store-url}")
    private String iosAppStoreUrl;

    @Value("${onboarding.app.android.store-url}")
    private String androidAppStoreUrl;

    @Value("${onboarding.app.fallback.url}")
    private String fallbackUrl;

    public String getAppStoreUrl(String deviceOs) {
        log.info("Redirecting device OS: {}", deviceOs);

        return switch (deviceOs.toLowerCase()) {
            case "ios" -> iosAppStoreUrl;
            case "android" -> androidAppStoreUrl;
            default -> {
                log.warn("Unknown device OS: {}, using fallback URL", deviceOs);
                yield fallbackUrl;
            }
        };
    }
}
