package com.sangwuny.toy.common;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		String message = ex.getReason() == null ? "Request failed." : ex.getReason();
		return ResponseEntity.status(status).body(Map.of("message", message, "error", status.getReasonPhrase()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
		FieldError firstError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
		String message = firstError == null ? "Invalid request." : firstError.getDefaultMessage();
		return ResponseEntity.badRequest().body(Map.of("message", message, "error", "Bad Request"));
	}
}
