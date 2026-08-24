package si.result.eearly.domain.dexcom;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DexcomToken {

	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("refresh_token")
	private String refreshToken;
	@JsonProperty("expires_in")
	private String expiresIn;
	@JsonProperty("token_type")
	private String tokenType;

	public String getBearer() {
		return String.format("Bearer %s", accessToken);
	}

	public DexcomTokenPayload getPayload() throws JsonProcessingException {
		String[] parts = accessToken.split("\\.");
		String payload = new String(java.util.Base64.getDecoder().decode(parts[1]));

		ObjectMapper objectMapper = new ObjectMapper();

		DexcomTokenPayload dexcomPayload = objectMapper.readValue(payload, DexcomTokenPayload.class);

		return dexcomPayload;
	}

	@Data
	public static class DexcomTokenPayload {
		String sub;
		String aud;
		List<String> scope;
		String iss;
		Long exp;
		Long iat;
		@JsonProperty("client_id")
		String clientId;

		public boolean isExpired() {
			return System.currentTimeMillis() / 1000 > exp;
		}
	}
}
