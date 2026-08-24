package si.result.eearly.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import si.result.eearly.genproto.GetDexcomAuthorizationUriRequest;
import si.result.eearly.genproto.GetDexcomAuthorizationUriResponse;

class ApiDeviceGrpcFacadeTest {

	@Test
	void buildsDexcomAuthorizationUriFromBackendConfiguration() {
		ApiDeviceGrpcFacade facade = new ApiDeviceGrpcFacade(null, null, null, null);
		ReflectionTestUtils.setField(facade, "dexcomBaseUri", "https://sandbox-api.dexcom.com");
		ReflectionTestUtils.setField(facade, "dexcomRedirectUri", "https://eearly.result.si/dexcom/oauth/callback");
		ReflectionTestUtils.setField(facade, "dexcomClientId", "client-id");

		GetDexcomAuthorizationUriResponse response = facade.getDexcomAuthorizationUri(
				GetDexcomAuthorizationUriRequest.newBuilder()
						.setState("state-value")
						.build());

		URI uri = URI.create(response.getAuthorizationUri());
		assertEquals("https", uri.getScheme());
		assertEquals("sandbox-api.dexcom.com", uri.getHost());
		assertEquals("/v3/oauth2/login", uri.getPath());
		assertEquals("https://eearly.result.si/dexcom/oauth/callback", response.getRedirectUri());

		String rawQuery = uri.getRawQuery();
		assertTrue(rawQuery.contains("client_id=client-id"));
		assertTrue(rawQuery.contains("redirect_uri=https%3A%2F%2Feearly.result.si%2Fdexcom%2Foauth%2Fcallback"));
		assertTrue(rawQuery.contains("response_type=code"));
		assertTrue(rawQuery.contains("scope=offline_access"));
		assertTrue(rawQuery.contains("state=state-value"));
	}
}
