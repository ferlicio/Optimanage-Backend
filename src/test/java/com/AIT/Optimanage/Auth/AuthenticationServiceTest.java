package com.AIT.Optimanage.Auth;

import com.AIT.Optimanage.Exceptions.InvalidTwoFactorCodeException;
import com.AIT.Optimanage.Exceptions.RefreshTokenInvalidException;
import com.AIT.Optimanage.Exceptions.RefreshTokenNotFoundException;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Support.TenantContext;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.LockedException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setupTenant() {
        TenantContext.setTenantId(1);
    }

    private RegisterRequest registerRequest() {
        return RegisterRequest.builder()
                .nome("John")
                .sobrenome("Doe")
                .email("john.doe@example.com")
                .senha("password")
                .build();
    }

    private AuthenticationRequest authRequest(String password) {
        return AuthenticationRequest.builder()
                .email("john.doe@example.com")
                .senha(password)
                .build();
    }

    private AuthenticationRequest authRequestWith2fa(String password, String code) {
        return AuthenticationRequest.builder()
                .email("john.doe@example.com")
                .senha(password)
                .twoFactorCode(code)
                .build();
    }

    @Test
    void registerCreatesUserAndReturnsTokens() {
        AuthenticationResponse response = authenticationService.register(registerRequest());
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }

    @Test
    void authenticateWithValidCredentialsReturnsTokens() {
        authenticationService.register(registerRequest());
        AuthenticationResponse response = authenticationService.authenticate(authRequest("password"));
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }

    @Test
    void authenticateWithInvalidPasswordThrows() {
        authenticationService.register(registerRequest());
        assertThatThrownBy(() -> authenticationService.authenticate(authRequest("wrong")))
                .isInstanceOf(org.springframework.security.core.AuthenticationException.class);
    }

    @Test
    void authenticateLockedUserThrows() {
        authenticationService.register(registerRequest());
        var user = userRepository.findByEmail("john.doe@example.com").orElseThrow();
        user.setLockoutExpiry(Instant.now().plusSeconds(60));
        userRepository.save(user);
        assertThatThrownBy(() -> authenticationService.authenticate(authRequest("password")))
                .isInstanceOf(LockedException.class);
    }

    @Test
    void twoFactorAuthenticationFlow() {
        authenticationService.register(registerRequest());
        TwoFactorToggleRequest toggleRequest = new TwoFactorToggleRequest();
        toggleRequest.setEmail("john.doe@example.com");
        toggleRequest.setEnable(true);
        authenticationService.toggleTwoFactor(toggleRequest);

        // login without code
        assertThatThrownBy(() -> authenticationService.authenticate(authRequest("password")))
                .isInstanceOf(InvalidTwoFactorCodeException.class);

        // login with valid code
        String secret = userRepository.findByEmail("john.doe@example.com").orElseThrow().getTwoFactorSecret();
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        String code = String.valueOf(gAuth.getTotpPassword(secret));
        AuthenticationResponse response = authenticationService.authenticate(authRequestWith2fa("password", code));
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    void refreshTokenFlow() {
        authenticationService.register(registerRequest());
        AuthenticationResponse auth = authenticationService.authenticate(authRequest("password"));
        AuthenticationResponse refreshed = authenticationService.refreshToken(auth.getRefreshToken());
        assertThat(refreshed.getToken()).isNotBlank();
    }

    @Test
    void refreshTokenExpiredThrows() {
        authenticationService.register(registerRequest());
        AuthenticationResponse auth = authenticationService.authenticate(authRequest("password"));
        var stored = refreshTokenRepository.findByToken(auth.getRefreshToken()).orElseThrow();
        stored.setExpiryDate(Instant.now().minusSeconds(1));
        refreshTokenRepository.save(stored);
        assertThatThrownBy(() -> authenticationService.refreshToken(auth.getRefreshToken()))
                .isInstanceOf(RefreshTokenInvalidException.class);
    }

    @Test
    void refreshTokenNotFoundThrows() {
        assertThatThrownBy(() -> authenticationService.refreshToken("missing"))
                .isInstanceOf(RefreshTokenNotFoundException.class);
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
