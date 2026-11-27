package si.result.eearly.resource.grpc;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.NotFoundException;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.dto.CreateUserDTO;
import si.result.eearly.dto.CreateUserResponseDTO;
import si.result.eearly.dto.OnboardingLinkDTO;
import si.result.eearly.exception.ExceptionResponse;
import si.result.eearly.exception.KeycloakException;
import si.result.eearly.facade.OnboardingServiceFacade;
import si.result.eearly.service.onboarding.OnboardingRateLimiter;


@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@Slf4j
public class OnboardingController {
  private final OnboardingServiceFacade onboardingServiceFacade;
  private final OnboardingRateLimiter rateLimiter;

  @PostMapping(path = "/create-user", produces = "application/json", consumes = "application/json")
  public ResponseEntity<?> createUser(@RequestBody final CreateUserDTO createUserDTO) {
    try {
      CreateUserResponseDTO response = onboardingServiceFacade.createUser(createUserDTO);
      return ResponseEntity.ok(response);
    } catch (ValidationException e) {
      log.error("Validation error when creating user", e);
      HttpStatus status = (e.getErrorCode() == ErrorCode.ALREADY_EXISTS)
              ? HttpStatus.CONFLICT
              : HttpStatus.BAD_REQUEST;
      return ResponseEntity
              .status(status)
              .body(new ExceptionResponse(status.value(), e.getMessage()));
    } catch (KeycloakException e) {
      log.error("Keycloak exception when creating user", e);
      HttpStatus status = HttpStatus.resolve(e.getStatusCode());
      if (status == null) {
        status = HttpStatus.INTERNAL_SERVER_ERROR;
      }
      return ResponseEntity
              .status(status)
              .body(new ExceptionResponse(status.value(), e.getMessage()));
    }
  }

  /** Issues a new onboarding link for an existing patient; earlier unused links stop working. */
  @PostMapping(path = "/users/{userId}/onboarding-link", produces = "application/json")
  public ResponseEntity<?> createOnboardingLink(@PathVariable final String userId) {
    UUID id;
    try {
      id = UUID.fromString(userId);
    } catch (IllegalArgumentException e) {
      return userNotFound();
    }
    try {
      OnboardingLinkDTO link = onboardingServiceFacade.issueOnboardingLink(id);
      return ResponseEntity.ok(link);
    } catch (NotFoundException e) {
      return userNotFound();
    }
  }

  /** Sends the browser to the app store. */
  @GetMapping(produces = "application/json")
  public ResponseEntity<?> getAppStoreURL(
      HttpServletRequest request) {
    if (!rateLimiter.tryAcquire(request)) {
      return tooManyRequests();
    }
    String userAgent = request.getHeader("User-Agent");
    return ResponseEntity.status(HttpStatus.FOUND)
        .header("Location", onboardingServiceFacade.getMobileStoreURL(userAgent))
        .build();
  }

  /**
   * The onboarding link opened in a browser: sends it to the app store. The code is neither
   * looked up nor used here, so link previews and scanners can never consume it; the app redeems
   * it with {@code /{code}/configuration}.
   */
  @GetMapping(value = "/{code}", produces = "application/json")
  public ResponseEntity<?> openOnboardingLink(
      HttpServletRequest request,
      @PathVariable String code) {
    return getAppStoreURL(request);
  }

  /**
   * Redeems a one-time onboarding code. Unknown, used, expired and malformed codes, and raw user
   * ids from the old link format, all get the same 404. A {@code token} query parameter, if
   * present, is ignored.
   */
  @GetMapping(value = "/{code}/configuration", produces = "application/json")
  public ResponseEntity<?> getConfiguration(
      HttpServletRequest request,
      @PathVariable String code) throws ValidationException {
    if (!rateLimiter.tryAcquire(request)) {
      return tooManyRequests();
    }
    return onboardingServiceFacade.getConfiguration(code)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(OnboardingController::notFound);
  }

  private static ResponseEntity<ExceptionResponse> notFound() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ExceptionResponse(HttpStatus.NOT_FOUND.value(), "Not found"));
  }

  private static ResponseEntity<ExceptionResponse> userNotFound() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ExceptionResponse(HttpStatus.NOT_FOUND.value(), "User not found"));
  }

  private ResponseEntity<ExceptionResponse> tooManyRequests() {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header("Retry-After", String.valueOf(rateLimiter.retryAfterSeconds()))
        .body(new ExceptionResponse(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests"));
  }
}
