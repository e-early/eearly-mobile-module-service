package si.result.eearly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.AccessPredicateConfigAttribute;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import si.result.eearly.genproto.BackofficeScheduleServiceGrpc;
import si.result.eearly.genproto.MeasurementServiceGrpc;
import si.result.eearly.genproto.ScheduleServiceGrpc;

class WebSecurityConfigurationTest {

  private static final String ADMIN_SERVICE_CLIENT_ID = "admin-service-test";
  private static final String PATIENT_CLIENT_ID = "mobile-app-test";

  private final AccessPredicate adminServiceOnly =
      WebSecurityConfiguration.adminServiceOnly(ADMIN_SERVICE_CLIENT_ID);

  @Test
  void adminServiceOnly_allowsAdminServiceToken() {
    assertThat(adminServiceOnly.test(jwtAuth(Map.of("client_id", ADMIN_SERVICE_CLIENT_ID)), null))
        .isTrue();
  }

  @Test
  void adminServiceOnly_deniesPatientToken() {
    assertThat(adminServiceOnly.test(jwtAuth(Map.of("azp", PATIENT_CLIENT_ID)), null)).isFalse();
  }

  @Test
  void adminServiceOnly_deniesOtherClient() {
    assertThat(adminServiceOnly.test(jwtAuth(Map.of("client_id", PATIENT_CLIENT_ID)), null))
        .isFalse();
  }

  @Test
  void adminServiceOnly_deniesAzpWithoutClientId() {
    assertThat(adminServiceOnly.test(jwtAuth(Map.of("azp", ADMIN_SERVICE_CLIENT_ID)), null))
        .isFalse();
  }

  @Test
  void adminServiceOnly_deniesNullAndAnonymous() {
    assertThat(adminServiceOnly.test(null, null)).isFalse();
    assertThat(adminServiceOnly.test(new AnonymousAuthenticationToken("key", "anonymous",
        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")), null)).isFalse();
  }

  @Test
  void adminServiceOnly_deniesUnauthenticated() {
    JwtAuthenticationToken auth = jwtAuth(Map.of("client_id", ADMIN_SERVICE_CLIENT_ID));
    auth.setAuthenticated(false);
    assertThat(adminServiceOnly.test(auth, null)).isFalse();
  }

  @Test
  void adminServiceOnly_deniesEverythingWhenClientIdNotConfigured() {
    Authentication auth = jwtAuth(Map.of("client_id", ADMIN_SERVICE_CLIENT_ID));
    assertThat(WebSecurityConfiguration.adminServiceOnly(null).test(auth, null)).isFalse();
    assertThat(WebSecurityConfiguration.adminServiceOnly("").test(auth, null)).isFalse();
  }

  @Test
  void metadataSource_restrictsGetMeasurementsForUserToAdminService() {
    GrpcSecurityMetadataSource source = metadataSource();

    assertAdminOnly(source, MeasurementServiceGrpc.getGetMeasurementsForUserMethod());
  }

  @Test
  void metadataSource_restrictsEveryBackofficeScheduleRpcToAdminService() {
    GrpcSecurityMetadataSource source = metadataSource();

    var methods = BackofficeScheduleServiceGrpc.getServiceDescriptor().getMethods();
    assertThat(methods).isNotEmpty();
    methods.forEach(method -> assertAdminOnly(source, method));
  }

  @Test
  void metadataSource_keepsPatientRpcsOpenToAuthenticatedPatients() {
    GrpcSecurityMetadataSource source = metadataSource();
    Authentication patient = jwtAuth(Map.of("azp", PATIENT_CLIENT_ID));

    AccessPredicate ownMeasurements =
        predicateFor(source, MeasurementServiceGrpc.getGetMeasurementsMethod());
    assertThat(ownMeasurements.test(patient, null)).isTrue();

    AccessPredicate createMeasurement =
        predicateFor(source, MeasurementServiceGrpc.getCreateMeasurementMethod());
    assertThat(createMeasurement.test(patient, null)).isTrue();
  }

  @Test
  void metadataSource_leavesScheduleNotificationRpcUnchanged() {
    GrpcSecurityMetadataSource source = metadataSource();
    Authentication patient = jwtAuth(Map.of("azp", PATIENT_CLIENT_ID));

    AccessPredicate sendNotification =
        predicateFor(source, ScheduleServiceGrpc.getSendScheduleNotificationMethod());
    assertThat(sendNotification.test(patient, null)).isTrue();
  }

  private GrpcSecurityMetadataSource metadataSource() {
    WebSecurityConfiguration configuration = new WebSecurityConfiguration();
    ReflectionTestUtils.setField(configuration, "adminServiceClientId", ADMIN_SERVICE_CLIENT_ID);
    return configuration.grpcSecurityMetadataSource();
  }

  private static void assertAdminOnly(GrpcSecurityMetadataSource source,
      MethodDescriptor<?, ?> method) {
    AccessPredicate predicate = predicateFor(source, method);
    String name = method.getFullMethodName();

    assertThat(predicate.test(jwtAuth(Map.of("client_id", ADMIN_SERVICE_CLIENT_ID)), null))
        .as("admin service token on %s", name).isTrue();
    assertThat(predicate.test(jwtAuth(Map.of("azp", PATIENT_CLIENT_ID)), null))
        .as("patient token on %s", name).isFalse();
    assertThat(predicate.test(null, null)).as("no token on %s", name).isFalse();
  }

  private static AccessPredicate predicateFor(GrpcSecurityMetadataSource source,
      MethodDescriptor<?, ?> method) {
    ServerCall<?, ?> call = mock(ServerCall.class);
    when(call.getMethodDescriptor()).thenAnswer(invocation -> method);
    Collection<ConfigAttribute> attributes = source.getAttributes(call);
    assertThat(attributes).singleElement().isInstanceOf(AccessPredicateConfigAttribute.class);
    return ((AccessPredicateConfigAttribute) attributes.iterator().next()).getAccessPredicate();
  }

  private static JwtAuthenticationToken jwtAuth(Map<String, Object> claims) {
    Jwt.Builder builder = Jwt.withTokenValue("test-token")
        .header("alg", "none")
        .subject("00000000-0000-0000-0000-000000000001")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(60));
    claims.forEach(builder::claim);
    return new JwtAuthenticationToken(builder.build(), List.of());
  }
}
