package si.result.eearly.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import si.result.eearly.domain.UserPrincipal;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.genproto.UpdateLanguageRequest;
import si.result.eearly.mapper.ProfileMapper;
import si.result.eearly.service.KeycloakService;
import si.result.eearly.service.ProfileService;
import si.result.eearly.service.UserPrincipalService;

/** UpdateLanguage acts on the caller's own profile only: the user comes from the token. */
class ProfileServiceGrpcFacadeLanguageTest {

  private static final String USER_ID = UUID.randomUUID().toString();

  private ProfileService profileService;
  private UserPrincipalService userPrincipalService;
  private ProfileServiceGrpcFacade facade;

  @BeforeEach
  void setUp() {
    profileService = mock(ProfileService.class);
    userPrincipalService = mock(UserPrincipalService.class);
    facade = new ProfileServiceGrpcFacade(profileService, userPrincipalService,
        mock(ProfileMapper.class), mock(KeycloakService.class));
  }

  @Test
  void updatesTheLanguageOfTheUserInTheToken() throws ValidationException {
    when(userPrincipalService.getUserPrincipal()).thenReturn(principal(USER_ID));
    Profile saved = Profile.createProfileFromId(USER_ID);
    saved.setLanguage("en");
    when(profileService.updateLanguage(USER_ID, "en")).thenReturn(saved);

    var response = facade.updateLanguage(UpdateLanguageRequest.newBuilder().setLanguage("en").build());

    assertThat(response.getLanguage()).isEqualTo("en");
    verify(profileService).updateLanguage(USER_ID, "en");
  }

  @Test
  void withoutAUserInTheToken_failsClosed() throws ValidationException {
    var request = UpdateLanguageRequest.newBuilder().setLanguage("en").build();

    when(userPrincipalService.getUserPrincipal()).thenReturn(null);
    assertThat(catchThrowable(() -> facade.updateLanguage(request)))
        .isInstanceOf(AccessDeniedException.class);

    when(userPrincipalService.getUserPrincipal()).thenReturn(principal(null));
    assertThat(catchThrowable(() -> facade.updateLanguage(request)))
        .isInstanceOf(AccessDeniedException.class);

    verify(profileService, never()).updateLanguage(any(), any());
  }

  @Test
  void mapsValidationErrorsToInvalidRequest() throws ValidationException {
    when(userPrincipalService.getUserPrincipal()).thenReturn(principal(USER_ID));
    when(profileService.updateLanguage(USER_ID, "de")).thenThrow(
        new ValidationException(ErrorCode.VALIDATION_ERROR, "language must be one of [sl, en]"));

    var e = catchThrowableOfType(ServiceInvalidRequestException.class,
        () -> facade.updateLanguage(UpdateLanguageRequest.newBuilder().setLanguage("de").build()));

    assertThat(e).isNotNull();
    assertThat(e.getErrorCode()).isEqualTo(si.result.eearly.genproto.ErrorCode.VALIDATION_ERROR);
  }

  private static UserPrincipal principal(String id) {
    UserPrincipal principal = new UserPrincipal();
    principal.setId(id);
    return principal;
  }
}
