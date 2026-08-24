package si.result.eearly.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPrincipal {
	@JsonProperty("sub")
	private String id;
	@JsonProperty("given_name")
	private String firstName;
	@JsonProperty("family_name")
	private String lastName;
	@JsonProperty("preferred_username")
	private String username;
	@JsonProperty("email")
	private String email;
}
