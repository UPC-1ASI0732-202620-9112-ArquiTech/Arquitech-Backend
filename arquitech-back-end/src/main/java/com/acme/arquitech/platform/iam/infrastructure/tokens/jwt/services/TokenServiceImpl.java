package com.acme.arquitech.platform.iam.infrastructure.tokens.jwt.services;
import com.acme.arquitech.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenServiceImpl implements BearerTokenService {
    private final SecretKey key;
    private final int expirationDays;
    public TokenServiceImpl(@Value("${authorization.jwt.secret}") String secret,
                            @Value("${authorization.jwt.expiration.days}") int expirationDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        if (expirationDays < 1) throw new IllegalArgumentException("JWT expiration days must be positive");
        this.expirationDays = expirationDays;
    }
    @Override
    public String generateToken(Authentication authentication) {
        return generateToken(authentication.getName());
    }
    @Override
    public String generateToken(String email) {
        var now = Instant.now();
        return Jwts.builder().subject(email).issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationDays, ChronoUnit.DAYS))).signWith(key).compact();
    }
    @Override
    public String getUsernameFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }
    @Override
    public boolean validateToken(String token) {
        try {
            String subject = getUsernameFromToken(token);
            return subject != null && !subject.isBlank();
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header != null && header.regionMatches(true, 0, "Bearer ", 0, 7) ? header.substring(7) : null;
    }
}
