package com.newsplatform.common.security;

import com.newsplatform.user.entity.Role;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.entity.UserStatus;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "a-development-secret-that-is-at-least-32-bytes-long",
            3600
    );

    @Test
    void tokenContainsOnlyIdentityRoleAndTimingClaims() throws Exception {
        User user = new User("Admin", "admin@example.com", "hash", Role.ADMIN, UserStatus.ACTIVE);
        UUID id = UUID.randomUUID();
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);

        String token = jwtService.generateToken(user);
        JWTClaimsSet claims = jwtService.parseToken(token);

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(claims.getSubject()).isEqualTo(id.toString());
        assertThat(claims.getStringClaim("role")).isEqualTo("ADMIN");
        assertThat(claims.getClaims().keySet()).containsExactlyInAnyOrder("sub", "role", "iat", "exp");
    }

    @Test
    void malformedTokenIsRejected() {
        assertThat(jwtService.isValid("not-a-jwt")).isFalse();
    }
}
