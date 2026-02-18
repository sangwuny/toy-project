package com.sangwuny.toy.auth;

public record TokenPair(
	String accessToken,
	String refreshToken,
	long expiresIn
) {
}
