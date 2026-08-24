package si.result.eearly.resource.grpc;

import jakarta.servlet.http.HttpServletRequest;
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
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.dto.CreateUserDTO;
import si.result.eearly.dto.CreateUserResponseDTO;
import si.result.eearly.dto.MobileConfigurationDTO;
import si.result.eearly.exception.ExceptionResponse;
import si.result.eearly.exception.KeycloakException;
import si.result.eearly.facade.OnboardingServiceFacade;


@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@Slf4j
public class OnboardingController {
  private final OnboardingServiceFacade onboardingServiceFacade;

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


  @GetMapping(produces = "application/json")
  public ResponseEntity<Void> getAppStoreURL(
      HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    return ResponseEntity.status(HttpStatus.FOUND)
        .header("Location", onboardingServiceFacade.getMobileStoreURL(userAgent))
        .build();
  }

  @GetMapping(value = "/{id}", produces = "application/json")
  public ResponseEntity<MobileConfigurationDTO> getConfiguration(
      HttpServletRequest request,
      @PathVariable String id) {
    String userAgent = request.getHeader("User-Agent");
    return ResponseEntity.status(HttpStatus.FOUND)
        .header("Location", onboardingServiceFacade.getMobileStoreURL(userAgent))
        .build();
  }

  @GetMapping(value = "/{id}/configuration", produces = "application/json")
  public ResponseEntity<MobileConfigurationDTO> getConfiguration(
      @PathVariable(name = "id") final String onboardingId ) throws ValidationException {
    return ResponseEntity.ok().body(onboardingServiceFacade.getConfiguration(onboardingId));
  }
}
