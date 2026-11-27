package si.result.eearly;

import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.AccessPredicateVoter;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import si.result.eearly.genproto.BackofficePatientServiceGrpc;
import si.result.eearly.genproto.BackofficeScheduleServiceGrpc;
import si.result.eearly.genproto.CaretakerServiceGrpc;
import si.result.eearly.genproto.FacilityServiceGrpc;
import si.result.eearly.genproto.MeasurementServiceGrpc;
import si.result.eearly.genproto.ProfileServiceGrpc;
import si.result.eearly.genproto.ScheduleServiceGrpc;
import io.grpc.ServerCall;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Profile(value = {"!public"})
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

  @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
  private String issuerUri;

  @Value("${keycloak.admin-service.client.id}")
  private String adminServiceClientId;

  @Value("${spring.security.cors.allowed-origins}")
  private List<String> allowedOrigins;

  @Value("${spring.security.cors.allowed-methods}")
  private List<String> allowedMethods;


  private final List<String> allowedHeaders = List.of("Authorization",
      "Content-Type",
      "Accept",
      "X-Requested-With",
      "Origin",
      "Access-Control-Request-Headers",
      "Access-Control-Request-Method",
      "User-Agent");


  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .exceptionHandling(exception -> exception
            .accessDeniedHandler(new CustomAccessDeniedHandler())
            .authenticationEntryPoint(new CustomAccessDeniedHandler()))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/.well-known/**")
            .permitAll()
            // Issuing onboarding links is for the admin service only (realm role onboard-patient).
            .requestMatchers("/api/v1/onboarding/create-user",
                "/api/v1/onboarding/users/*/onboarding-link")
            .hasRole("ONBOARD-PATIENT")
            // The app store redirect and the redemption of a one-time code are anonymous. The
            // controller answers everything that is not a valid code with the same 404.
            .requestMatchers(HttpMethod.GET, "/api/v1/onboarding",
                "/api/v1/onboarding/*", "/api/v1/onboarding/*/configuration")
            .permitAll()
            .anyRequest()
            .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
            .authenticationEntryPoint(new CustomAccessDeniedHandler())
            .jwt(jwt -> jwt.jwtAuthenticationConverter(
                new KeycloakJwtAuthenticationConverter("ROLE_"))));

    http.csrf(AbstractHttpConfigurer::disable);
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

    return http.build();
  }

  @Bean
  public GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
    final ManualGrpcSecurityMetadataSource source = new ManualGrpcSecurityMetadataSource();

    AccessPredicate adminServiceOnly = adminServiceOnly(adminServiceClientId);

    // Only the admin service (client credentials) may call these RPCs: they take a
    // caller-supplied user id or manage schedules on behalf of caretakers. Patients use
    // GetMeasurements, which resolves the user from the token.
    source.set(BackofficeScheduleServiceGrpc.getServiceDescriptor(), adminServiceOnly);
    // The admin service owns patient demographics and syncs them here for a caller-supplied user id.
    source.set(BackofficePatientServiceGrpc.getServiceDescriptor(), adminServiceOnly);
    source.set(MeasurementServiceGrpc.getGetMeasurementsForUserMethod(), adminServiceOnly);
    // The admin service triggers these pushes when a schedule is created or a reminder is due.
    // They take a caller-supplied schedule id, so only the admin service may call them.
    source.set(ScheduleServiceGrpc.getSendScheduleNotificationMethod(), adminServiceOnly);
    source.set(ScheduleServiceGrpc.getSendMeasurementReminderMethod(), adminServiceOnly);
    // These act on caller-supplied devices or records. Only the admin service may call them
    // (it syncs caretakers and facilities).
    source.set(ProfileServiceGrpc.getSendFCMNotificationForTokenMethod(), adminServiceOnly);
    source.set(CaretakerServiceGrpc.getUpsertCaretakerMethod(), adminServiceOnly);
    source.set(FacilityServiceGrpc.getUpsertFacilityMethod(), adminServiceOnly);

    return source.setDefault(AccessPredicate.authenticated());
  }

  static AccessPredicate adminServiceOnly(String adminServiceClientId) {
    return (Authentication authentication, ServerCall<?, ?> call) -> {
      if (authentication == null || !authentication.isAuthenticated()
          || authentication instanceof AnonymousAuthenticationToken) {
        return false;
      }

      if (adminServiceClientId == null || adminServiceClientId.isBlank()) {
        return false;
      }

      return authentication.getPrincipal() instanceof Jwt jwt
          && adminServiceClientId.equals(jwt.getClaimAsString("client_id"));
    };
  }

  // https://github.com/grpc-ecosystem/grpc-spring/issues/1068
  @Bean
  public AccessDecisionManager accessDecisionManager() {
    final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
    voters.add(new AccessPredicateVoter());
    return new UnanimousBased(voters);
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(
        jwtDecoder());
    jwtAuthenticationProvider.setJwtAuthenticationConverter(
        new KeycloakJwtAuthenticationConverter(""));

    return new ProviderManager(List.of(jwtAuthenticationProvider));
  }

  @Bean
  public GrpcAuthenticationReader grpcAuthenticationReader() {
    return new BearerAuthenticationReader(BearerTokenAuthenticationToken::new);
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return JwtDecoders.fromOidcIssuerLocation(issuerUri);
  }

  private UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(allowedOrigins);
    configuration.setAllowedMethods(allowedMethods);
    configuration.setAllowedHeaders(allowedHeaders);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}