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
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authorization.AuthorizationDecision;
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
import si.result.eearly.genproto.BackofficeScheduleServiceGrpc;
import si.result.eearly.genproto.MeasurementServiceGrpc;
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
            .requestMatchers("/api/v1/onboarding/create-user")
            .hasRole("ONBOARD-PATIENT")
            .requestMatchers("/eearly-admin-service/api/v1/onboarding/**").permitAll()
            .requestMatchers("api/v1/onboarding/**").permitAll()
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

    AccessPredicate adminServiceOrAuthenticated = (Authentication authentication, ServerCall<?, ?> call) -> {

      if (authentication == null || !authentication.isAuthenticated()) {
        return false;
      }

      if (authentication instanceof AnonymousAuthenticationToken) {
        return false;
      }

      Object principal = authentication.getPrincipal();
      if (principal instanceof Jwt jwt) {
        String clientId = jwt.getClaim("client_id");
        if (adminServiceClientId != null && adminServiceClientId.equals(clientId)) {
          return true;
        }
      }
      
      return true;
    };

    source.set(BackofficeScheduleServiceGrpc.getServiceDescriptor(), adminServiceOrAuthenticated);
    source.set(MeasurementServiceGrpc.getGetMeasurementsForUserMethod(), adminServiceOrAuthenticated);
    
    return source.setDefault(AccessPredicate.authenticated());
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