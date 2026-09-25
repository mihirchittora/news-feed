package com.newsplatform.common.security;

import com.newsplatform.user.entity.User;
import com.newsplatform.user.entity.UserStatus;
import com.newsplatform.user.repository.UserRepository;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();
        if (token.isBlank() || !jwtService.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JWTClaimsSet claims = jwtService.parseToken(token);
            User user = userRepository.findWithRolesById(jwtService.getUserId(claims)).orElse(null);

            if (user != null && user.getStatus() == UserStatus.ACTIVE
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getRole());
                java.util.Set<String> permissions = new java.util.HashSet<>();
                boolean superAdmin = user.getRoles().stream().anyMatch(role -> role.getStatus().name().equals("ACTIVE") && role.getCode().equals("SUPER_ADMIN"));
                user.getRoles().stream()
                        .filter(role -> role.getStatus().name().equals("ACTIVE"))
                        .flatMap(role -> role.getPermissions().stream())
                        .map(permission -> permission.getCode())
                        .forEach(permissions::add);
                if (superAdmin) {
                    // SUPER_ADMIN is seeded with the full catalog; the explicit authority also protects against seed drift.
                    permissions.addAll(user.getRoles().stream()
                            .flatMap(role -> role.getPermissions().stream())
                            .map(permission -> permission.getCode())
                            .toList());
                }
                java.util.List<SimpleGrantedAuthority> authorities = permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
                if (superAdmin) authorities.add(new SimpleGrantedAuthority("SUPER_ADMIN"));
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (RuntimeException ignored) {
            // Invalid claims are treated as an unauthenticated request without exposing token details.
        }

        filterChain.doFilter(request, response);
    }
}
