package si.result.eearly;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
	private String rolePrefix;

	@Override
	public AbstractAuthenticationToken convert(Jwt source) {
		return new JwtAuthenticationToken(source,
						Stream.concat(
														new JwtGrantedAuthoritiesConverter().convert(source).stream(), extractResourceRoles(source).stream())
										.collect(Collectors.toSet()));
	}

	private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
		Map<String, Object> realmAccess = jwt.getClaim("realm_access");
		if (realmAccess == null) {
			return Collections.emptySet();
		}

		Object rolesObject = realmAccess.get("roles");
		if (rolesObject instanceof List<?> rolesList) {
			return rolesList.stream()
							.filter(String.class::isInstance)
							.map(String.class::cast)
							.map(r -> new SimpleGrantedAuthority(rolePrefix + r.toUpperCase()))
							.collect(Collectors.toSet());
		}

		return Collections.emptySet();
	}
}
