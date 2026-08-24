package si.result.eearly.dto;

import java.util.UUID;

public record MobileConfigurationDTO(

        String keycloakBaseUrl,

        String keycloakRealm,

        String keycloakClientId,

        String host,

        UUID userId,

        String accessToken,

        String refreshToken,

        String apiBaseUrl
){}
