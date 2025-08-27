package com.AIT.Optimanage.Auth;

import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                refreshTokenRepository,
                tokenBlacklistService);
    }

    @Test
    void logoutShouldDeleteRefreshTokenAndBlacklistToken() {
        String token = "jwt";
        String email = "user@example.com";
        User user = new User();

        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        authenticationService.logout(token);

        verify(refreshTokenRepository).deleteByUser(user);
        verify(tokenBlacklistService).blacklistToken(token);
    }
}
