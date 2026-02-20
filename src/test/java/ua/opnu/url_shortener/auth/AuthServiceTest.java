package ua.opnu.url_shortener.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.opnu.url_shortener.auth.dto.LoginRequest;
import ua.opnu.url_shortener.auth.dto.RegisterRequest;
import ua.opnu.url_shortener.auth.entity.User;
import ua.opnu.url_shortener.auth.exception.UserAlreadyExistsException;
import ua.opnu.url_shortener.auth.repository.UserRepository;
import ua.opnu.url_shortener.auth.service.AuthService;
import ua.opnu.url_shortener.auth.service.JwtService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldThrowException_WhenUserExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldSaveUser_WhenUserIsNew() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new_user");
        request.setPassword("pass123");

        when(userRepository.existsByUsername("new_user")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed_pass");

        authService.register(request);

        verify(passwordEncoder).encode("pass123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("admin", "password");

        User user = new User();
        user.setUsername("admin");
        user.setPassword("hashed_pass");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed_pass")).thenReturn(true);
        when(jwtService.generateToken("admin")).thenReturn("access_token_123");

        String token = authService.login(request);

        assertEquals("access_token_123", token);
        verify(jwtService).generateToken("admin");
    }
}