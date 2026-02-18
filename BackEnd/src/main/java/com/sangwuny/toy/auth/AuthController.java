package com.sangwuny.toy.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sangwuny.toy.auth.dto.AuthResponse;
import com.sangwuny.toy.auth.dto.AuthUserResponse;
import com.sangwuny.toy.auth.dto.LoginRequest;
import com.sangwuny.toy.auth.dto.SignupRequest;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;
	private final boolean refreshCookieSecure;

	public AuthController(
		AuthService authService,
		@Value("${app.auth.refresh-cookie-secure:false}") boolean refreshCookieSecure
	) {
		this.authService = authService;
		this.refreshCookieSecure = refreshCookieSecure;
	}

	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
		AuthResponse response = authService.signup(request);
		return ResponseEntity.ok()
			.header("Set-Cookie", createRefreshCookie(response.refreshToken(), response.expiresIn()))
			.body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthResponse response = authService.login(request);
		return ResponseEntity.ok()
			.header("Set-Cookie", createRefreshCookie(response.refreshToken(), response.expiresIn()))
			.body(response);
	}

	@GetMapping("/me")
	public ResponseEntity<AuthUserResponse> me(Authentication authentication) {
		Long userId = Long.parseLong(authentication.getName());
		return ResponseEntity.ok(authService.getCurrentUser(userId));
	}

	private String createRefreshCookie(String refreshToken, long maxAgeSeconds) {
		return ResponseCookie.from("refreshToken", refreshToken)
			.httpOnly(true)
			.secure(refreshCookieSecure)
			.path("/")
			.sameSite("Lax")
			.maxAge(maxAgeSeconds)
			.build()
			.toString();
	}
}
