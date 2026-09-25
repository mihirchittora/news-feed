package com.newsplatform.auth;

import com.newsplatform.auth.dto.LoginRequest;
import com.newsplatform.auth.dto.LoginResponse;
import com.newsplatform.auth.dto.RegisterRequest;
import com.newsplatform.auth.service.AuthService;
import com.newsplatform.common.error.DuplicateEmailException;
import com.newsplatform.common.error.InvalidCredentialsException;
import com.newsplatform.common.security.JwtService;
import com.newsplatform.user.entity.Role;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.entity.UserStatus;
import com.newsplatform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerNormalizesEmailHashesPasswordAndForcesUserRole() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.register(new RegisterRequest(" John Doe ", "JOHN@EXAMPLE.COM ", "Password123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("John Doe");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$hashed");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.email()).isEqualTo("john@example.com");
        verify(passwordEncoder).encode("Password123");
    }

    @Test
    void duplicateRegistrationIsRejected() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("John", "john@example.com", "Password123")))
                .isInstanceOf(DuplicateEmailException.class);
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void loginReturnsJwtForActiveUser() {
        User user = new User("John Doe", "john@example.com", "hashed", Role.USER, UserStatus.ACTIVE);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        LoginResponse response = authService.login(new LoginRequest("john@example.com", "Password123"));

        assertThat(response.accessToken()).isEqualTo("jwt");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600);
        assertThat(response.user().role()).isEqualTo("USER");
    }

    @Test
    void invalidCredentialsAreRejectedWithoutRevealingAccountState() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("john@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void disabledUserCannotLogin() {
        User user = new User("John Doe", "john@example.com", "hashed", Role.USER, UserStatus.DISABLED);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("john@example.com", "Password123")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(passwordEncoder, never()).matches(any(), any());
    }
}
