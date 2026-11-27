package si.result.eearly.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import si.result.eearly.admin.AdminServiceClient;
import si.result.eearly.ehr.EhrbaseClient;
import si.result.eearly.repository.ProfileRepository;
import si.result.eearly.service.push.PushNotificationService;

class ProfileServicePushNotificationTest {

  @Test
  void sendNotificationForToken_delegatesToThePushPortAndReturnsItsResult() {
    PushNotificationService push = mock(PushNotificationService.class);
    when(push.sendNotificationForToken("token-1", "Title", "Body")).thenReturn("message-42");
    ProfileServiceImpl profileService = newService(push);

    String result = profileService.sendNotificationForToken("token-1", "Title", "Body");

    assertThat(result).isEqualTo("message-42");
    verify(push).sendNotificationForToken("token-1", "Title", "Body");
  }

  @Test
  void sendNotificationForToken_returnsEmptyWhenThePortSendsNothing() {
    PushNotificationService push = mock(PushNotificationService.class);
    when(push.sendNotificationForToken("token-1", "Title", "Body")).thenReturn("");
    ProfileServiceImpl profileService = newService(push);

    assertThat(profileService.sendNotificationForToken("token-1", "Title", "Body")).isEmpty();
  }

  private static ProfileServiceImpl newService(PushNotificationService push) {
    return new ProfileServiceImpl(mock(ProfileRepository.class), mock(EhrbaseClient.class), Clock.systemUTC(),
        mock(AdminServiceClient.class), push);
  }
}
