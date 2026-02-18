package com.sangwuny.toy.auth;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sangwuny.toy.auth.dto.AuthResponse;
import com.sangwuny.toy.auth.dto.AuthUserResponse;
import com.sangwuny.toy.auth.dto.LoginRequest;
import com.sangwuny.toy.auth.dto.SignupRequest;
import com.sangwuny.toy.domain.user.User;
import com.sangwuny.toy.domain.user.UserRepository;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
	}

	public AuthResponse signup(SignupRequest request) {
		String normalizedEmail = normalizeEmail(request.email());
		if (userRepository.existsByEmail(normalizedEmail)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
		}

		User user = new User();
		user.setName(request.name().trim());
		user.setEmail(normalizedEmail);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		User saved = userRepository.save(user);

		TokenPair tokens = tokenService.issueTokens(saved, true);
		return new AuthResponse(
			tokens.accessToken(),
			tokens.refreshToken(),
			"Bearer",
			tokens.expiresIn(),
			toUserResponse(saved),
			"Signup completed."
		);
	}

	public AuthResponse login(LoginRequest request) {
		String normalizedEmail = normalizeEmail(request.email());
		User user = userRepository.findByEmail(normalizedEmail)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
		}

		TokenPair tokens = tokenService.issueTokens(user, request.remember());
		return new AuthResponse(
			tokens.accessToken(),
			tokens.refreshToken(),
			"Bearer",
			tokens.expiresIn(),
			toUserResponse(user),
			"Login successful."
		);
	}

	public AuthUserResponse getCurrentUser(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token."));
		return toUserResponse(user);
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private AuthUserResponse toUserResponse(User user) {
		return new AuthUserResponse(user.getId(), user.getName(), user.getEmail());
	}
}
