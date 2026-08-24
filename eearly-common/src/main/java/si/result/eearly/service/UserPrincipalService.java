package si.result.eearly.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;
import si.result.eearly.domain.UserPrincipal;

@Slf4j
@Service
public class UserPrincipalService {

	public UserPrincipal getUserPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		UserPrincipal userPrincipal = null;

		if (authentication.getPrincipal() instanceof Jwt) {
			Jwt jwt = (Jwt) authentication.getPrincipal();
			log.debug("jwt: {}", jwt.getClaims());

			Map<String, Object> claims = new HashMap<>(jwt.getClaims());

			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			userPrincipal = mapper.convertValue(claims, UserPrincipal.class);
		}

		return userPrincipal;
	}
}
