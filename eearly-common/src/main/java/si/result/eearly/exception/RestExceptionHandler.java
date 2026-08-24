package si.result.eearly.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import si.result.eearly.domain.exception.ValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ExceptionResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
		log.error(ex.getMessage(), ex);

		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(new ExceptionResponse(
						HttpStatus.NOT_FOUND.value(),
						ex.getMessage()));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ExceptionResponse> handleMissingServletRequestParameterException(
			MissingServletRequestParameterException ex) {
		log.error(ex.getMessage(), ex);

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(new ExceptionResponse(
						HttpStatus.BAD_REQUEST.value(),
						ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(
			MethodArgumentTypeMismatchException ex) {
		log.error(ex.getMessage(), ex);

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(new ExceptionResponse(
						HttpStatus.BAD_REQUEST.value(),
						ex.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		log.error(ex.getMessage(), ex);

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(new ExceptionResponse(
						HttpStatus.BAD_REQUEST.value(),
						ex.getMessage()));
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ExceptionResponse> handleValidationException(ValidationException ex) {
		log.info(ex.getMessage(), ex);

		String code = String.valueOf(ex.getErrorCode());
		HttpStatus status = "ALREADY_EXISTS".equals(code)
				? HttpStatus.CONFLICT
				: HttpStatus.BAD_REQUEST;

		return ResponseEntity
				.status(status)
				.body(new ExceptionResponse(status.value(), ex.getMessage()));
	}
}
