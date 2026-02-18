package com.sangwuny.toy.auth.dto;

public record AuthUserResponse(
	Long id,
	String name,
	String email
) {
}
