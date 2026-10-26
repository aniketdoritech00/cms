package in.doritech.cms.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey secretKey;
	private final long expiration;

	public JwtService(@Value("${app.jwtSecret}") String secret, @Value("${app.jwtExpirationMs}") long expiration) {

		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

		this.expiration = expiration;
	}

	public String generateToken(Long employeeId) {

		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expiration);

		return Jwts.builder().subject(String.valueOf(employeeId)).issuedAt(now).expiration(expiryDate)
				.signWith(secretKey).compact();
	}

	public Long extractEmployeeId(String token) {

		String employeeId = extractAllClaims(token).getSubject();

		return Long.valueOf(employeeId);
	}

	public boolean isTokenValid(String token) {

		Claims claims = extractAllClaims(token);

		return claims.getSubject() != null && claims.getExpiration() != null
				&& claims.getExpiration().after(new Date());
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
	}

	public long getExpiration() {
		return expiration;
	}
}