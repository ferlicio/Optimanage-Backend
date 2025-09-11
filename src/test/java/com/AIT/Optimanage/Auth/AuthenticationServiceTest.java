package com.AIT.Optimanage.Auth;

import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Support.EmailService;
import com.AIT.Optimanage.Config.JwtService;
import com.AIT.Optimanage.Config.AuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Mock
    private EmailService emailService;
    @Mock
    private AuthProperties authProperties;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                refreshTokenRepository,
                tokenBlacklistService,
                emailService,
                authProperties);
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

    @Test
    void authenticateSkipsTwoFactorWhenSecretMissing() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .senha("password")
                .build();
        User user = new User();
        user.setEmail("user@example.com");
        user.setSenha("encoded");
        user.setTwoFactorEnabled(true);
        user.setTenantId(1);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtService.generateToken(anyMap(), eq(user))).thenReturn("jwt");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh");
        when(jwtService.getRefreshExpiration()).thenReturn(1000L);
        when(userRepository.save(user)).thenReturn(user);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertEquals("jwt", response.getToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void refreshTokenGeneratesNewTokenAndRevokesOld() {
        String oldToken = "oldRefresh";
        User user = new User();
        user.setTenantId(1);
        RefreshToken storedToken = RefreshToken.builder()
                .token(oldToken)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(60))
                .revoked(false)
                .build();

        when(refreshTokenRepository.revokeIfNotRevoked(oldToken)).thenReturn(1);
        when(refreshTokenRepository.findByToken(oldToken)).thenReturn(Optional.of(storedToken));
        when(jwtService.isTokenValid(oldToken, user)).thenReturn(true);
        when(jwtService.generateToken(anyMap(), eq(user))).thenReturn("newJwt");
        when(jwtService.generateRefreshToken(user)).thenReturn("newRefresh");
        when(jwtService.getRefreshExpiration()).thenReturn(1000L);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthenticationResponse response = authenticationService.refreshToken(oldToken);

        assertEquals("newJwt", response.getToken());
        assertEquals("newRefresh", response.getRefreshToken());
        verify(refreshTokenRepository).revokeIfNotRevoked(oldToken);
    }
}
