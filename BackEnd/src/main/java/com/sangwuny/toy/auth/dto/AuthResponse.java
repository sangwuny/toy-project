package com.sangwuny.toy.auth.dto;

public record AuthResponse(
	String accessToken,
	String refreshToken,
	String tokenType,
	long expiresIn,
	AuthUserResponse user,
	String message
) {
}
