package si.result.eearly;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler, AuthenticationEntryPoint {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exc)
			throws IOException {

		sendErrorResponse(response, HttpStatus.FORBIDDEN.value(),
				new AuthenticationExceptionResponse(
						HttpStatus.FORBIDDEN.value(), "Access Denied"));
	};

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {

		sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
				new AuthenticationExceptionResponse(
						HttpStatus.UNAUTHORIZED.value(), "Unauthorized"));
	}

	private void sendErrorResponse(HttpServletResponse response, int status, AuthenticationExceptionResponse responseBody)
			throws IOException {

		response.setStatus(status);
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		ObjectMapper objectMapper = new ObjectMapper();
		String responseBodyJson = objectMapper.writeValueAsString(responseBody);

		response.getOutputStream().write(responseBodyJson.getBytes(StandardCharsets.UTF_8));
	}
}
