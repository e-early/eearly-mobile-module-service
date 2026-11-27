package si.result.eearly.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.test.util.ReflectionTestUtils;

class KeycloakServiceUserExistsTest {

  private final Keycloak keycloak = mock(Keycloak.class, RETURNS_DEEP_STUBS);
  private final KeycloakServiceImpl service = new KeycloakServiceImpl(keycloak);
  private final UUID userId = UUID.randomUUID();

  KeycloakServiceUserExistsTest() {
    ReflectionTestUtils.setField(service, "keycloakRealm", "test-realm");
  }

  @Test
  void existingUser() {
    when(keycloak.realm("test-realm").users().get(userId.toString()).toRepresentation())
        .thenReturn(new UserRepresentation());

    assertThat(service.userExists(userId)).isTrue();
  }

  @Test
  void unknownUser() {
    when(keycloak.realm("test-realm").users().get(userId.toString()).toRepresentation())
        .thenThrow(new NotFoundException());

    assertThat(service.userExists(userId)).isFalse();
  }
}
