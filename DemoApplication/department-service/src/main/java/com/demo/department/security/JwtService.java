package com.demo.department.security;

import com.demo.department.config.Constants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.expiration-minutes:60}")
    private long expiration;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String issue(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder().subject(username).claim(Constants.ROLE, role).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiration * 60))).signWith(key()).compact();
    }

    public String role(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().get(Constants.ROLE, String.class);
    }
}
