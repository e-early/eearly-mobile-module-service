package si.result.eearly.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import si.result.eearly.genproto.SendFCMNotificationForTokenRequest;
import si.result.eearly.genproto.SendFCMNotificationForTokenResponse;
import si.result.eearly.mapper.ProfileMapper;
import si.result.eearly.service.KeycloakService;
import si.result.eearly.service.ProfileService;
import si.result.eearly.service.UserPrincipalService;

@ExtendWith(MockitoExtension.class)
class ProfileServiceGrpcFacadeTest {

  @Mock private ProfileService profileService;
  @Mock private UserPrincipalService userPrincipalService;
  @Mock private ProfileMapper profileMapper;
  @Mock private KeycloakService keycloakService;

  @InjectMocks private ProfileServiceGrpcFacade facade;

  private static final SendFCMNotificationForTokenRequest REQUEST = SendFCMNotificationForTokenRequest.newBuilder()
      .setToken("token-1")
      .setTitle("Title")
      .setBody("Body")
      .build();

  @Test
  void sendNotificationForToken_returnsEmptyMessageWhenServiceReturnsNull() {
    when(profileService.sendNotificationForToken(anyString(), any(), any())).thenReturn(null);

    SendFCMNotificationForTokenResponse response = facade.sendNotificationForToken(REQUEST);

    assertThat(response.getFcmReturnMessage()).isEmpty();
  }

  @Test
  void sendNotificationForToken_returnsTheProviderMessageId() {
    when(profileService.sendNotificationForToken(anyString(), any(), any())).thenReturn("message-42");

    SendFCMNotificationForTokenResponse response = facade.sendNotificationForToken(REQUEST);

    assertThat(response.getFcmReturnMessage()).isEqualTo("message-42");
  }
}
