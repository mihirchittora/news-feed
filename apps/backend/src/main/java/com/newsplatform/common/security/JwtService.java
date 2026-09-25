package com.newsplatform.common.security;

import com.newsplatform.user.entity.User;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final byte[] signingSecret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must be at least 32 bytes long");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("JWT_EXPIRATION must be greater than zero");
        }
        this.signingSecret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(User user) {
        Instant issuedAt = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(issuedAt.plusSeconds(expirationSeconds)))
                .build();
        try {
            SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            JWSSigner signer = new MACSigner(signingSecret);
            signedJwt.sign(signer);
            return signedJwt.serialize();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create access token", exception);
        }
    }

    public JWTClaimsSet parseToken(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(signingSecret);
            if (!signedJwt.verify(verifier)) {
                throw new IllegalArgumentException("Invalid token signature");
            }
            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
                throw new IllegalArgumentException("Token has expired");
            }
            return claims;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid token", exception);
        }
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public UUID getUserId(JWTClaimsSet claims) {
        return UUID.fromString(claims.getSubject());
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
