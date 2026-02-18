package com.sangwuny.toy.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sangwuny.toy.domain.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenService {

	private final byte[] secretKey;
	private final long accessExpirationSeconds;
	private final long refreshExpirationSeconds;

	public TokenService(
		@Value("${app.jwt.secret}") String secret,
		@Value("${app.jwt.access-expiration-seconds}") long accessExpirationSeconds,
		@Value("${app.jwt.refresh-expiration-seconds}") long refreshExpirationSeconds
	) {
		this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
		this.accessExpirationSeconds = accessExpirationSeconds;
		this.refreshExpirationSeconds = refreshExpirationSeconds;
	}

	public TokenPair issueTokens(User user, boolean remember) {
		long refreshExpiresIn = remember ? refreshExpirationSeconds : Math.min(refreshExpirationSeconds, 86400L);
		return new TokenPair(
			createToken(user, "access", accessExpirationSeconds),
			createToken(user, "refresh", refreshExpiresIn),
			accessExpirationSeconds
		);
	}

	public Claims parseAccessToken(String token) throws JwtException {
		Jws<Claims> claims = Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(secretKey))
			.build()
			.parseSignedClaims(token);

		if (!"access".equals(claims.getPayload().get("type", String.class))) {
			throw new JwtException("Invalid token type.");
		}
		return claims.getPayload();
	}

	private String createToken(User user, String type, long expiresInSeconds) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(expiresInSeconds);

		return Jwts.builder()
			.subject(String.valueOf(user.getId()))
			.claim("type", type)
			.claim("email", user.getEmail())
			.claim("name", user.getName())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.signWith(Keys.hmacShaKeyFor(secretKey), Jwts.SIG.HS256)
			.compact();
	}
}
