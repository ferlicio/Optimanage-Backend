package com.AIT.Optimanage.Auth;

import com.AIT.Optimanage.Config.AuthProperties;
import com.AIT.Optimanage.Config.JwtService;
import com.AIT.Optimanage.Exceptions.InvalidTwoFactorCodeException;
import com.AIT.Optimanage.Exceptions.RefreshTokenInvalidException;
import com.AIT.Optimanage.Models.Organization.UserInvite;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Services.Organization.UserInviteService;
import com.AIT.Optimanage.Support.EmailService;
import com.AIT.Optimanage.Support.TenantContext;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserInviteService userInviteService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private EmailService emailService;

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final AuthProperties authProperties = new AuthProperties();
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private AuthenticationService authenticationService;

    private final Map<String, User> usersByEmail = new HashMap<>();
    private final Map<String, RefreshToken> refreshTokensByValue = new HashMap<>();
    private final AtomicInteger userIdSequence = new AtomicInteger(1);
    private final AtomicInteger refreshIdSequence = new AtomicInteger(1);
    private final AtomicInteger refreshTokenSequence = new AtomicInteger();

    @BeforeEach
    void setup() {
        authenticationService = new AuthenticationService(
                userRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                refreshTokenRepository,
                tokenBlacklistService,
                emailService,
                authProperties,
                meterRegistry,
                userInviteService
        );

        TenantContext.setTenantId(1);
        usersByEmail.clear();
        refreshTokensByValue.clear();
        userIdSequence.set(1);
        refreshIdSequence.set(1);
        refreshTokenSequence.set(0);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(userIdSequence.getAndIncrement());
            }
            usersByEmail.put(user.getEmail(), user);
            return user;
        });
        when(userRepository.findByEmail(anyString())).thenAnswer(invocation ->
                Optional.ofNullable(usersByEmail.get(invocation.getArgument(0))));

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            if (token.getId() == null) {
                token.setId(refreshIdSequence.getAndIncrement());
            }
            refreshTokensByValue.put(token.getToken(), token);
            return token;
        });
        when(refreshTokenRepository.findByToken(anyString())).thenAnswer(invocation ->
                Optional.ofNullable(refreshTokensByValue.get(invocation.getArgument(0))));
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            refreshTokensByValue.values().removeIf(token -> token.getUser().equals(user));
            return null;
        }).when(refreshTokenRepository).deleteByUser(any(User.class));
        when(refreshTokenRepository.revokeIfNotRevoked(anyString())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            RefreshToken token = refreshTokensByValue.get(value);
            if (token != null && !token.isRevoked()) {
                token.setRevoked(true);
                return 1;
            }
            return 0;
        });

        when(userInviteService.validarConvite(anyString(), anyString())).thenAnswer(invocation -> {
            UserInvite invite = UserInvite.builder()
                    .code("INVITE-CODE")
                    .role(Role.ADMIN)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                    .build();
            invite.setTenantId(1);
            return invite;
        });
        lenient().doNothing().when(userInviteService).marcarComoUsado(any(), any());

        when(jwtService.generateRefreshToken(any(User.class))).thenAnswer(invocation ->
                "refresh-token-" + refreshTokenSequence.incrementAndGet());
        when(jwtService.generateToken(anyMap(), any(User.class))).thenAnswer(invocation ->
                "jwt-token-" + UUID.randomUUID());
        when(jwtService.getRefreshExpiration()).thenReturn(3_600_000L);
        when(jwtService.isTokenValid(anyString(), any(User.class))).thenReturn(true);

        when(authenticationManager.authenticate(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    private RegisterRequest registerRequest() {
        return RegisterRequest.builder()
                .nome("John")
                .sobrenome("Doe")
                .email("john.doe@example.com")
                .senha("password")
                .codigoConvite("INVITE-CODE")
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
        assertThat(usersByEmail).containsKey("john.doe@example.com");
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
        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager)
                .authenticate(argThat(auth -> auth instanceof UsernamePasswordAuthenticationToken token
                        && "wrong".equals(token.getCredentials())));

        assertThatThrownBy(() -> authenticationService.authenticate(authRequest("wrong")))
                .isInstanceOf(org.springframework.security.core.AuthenticationException.class);
    }

    @Test
    void authenticateLockedUserThrows() {
        authenticationService.register(registerRequest());
        User user = usersByEmail.get("john.doe@example.com");
        user.setLockoutExpiry(Instant.now().plusSeconds(60));

        assertThatThrownBy(() -> authenticationService.authenticate(authRequest("password")))
                .isInstanceOf(org.springframework.security.authentication.LockedException.class);
    }

    @Test
    void twoFactorAuthenticationFlow() {
        authenticationService.register(registerRequest());
        TwoFactorToggleRequest toggleRequest = new TwoFactorToggleRequest();
        toggleRequest.setEmail("john.doe@example.com");
        toggleRequest.setEnable(true);
        authenticationService.toggleTwoFactor(toggleRequest);

        assertThatThrownBy(() -> authenticationService.authenticate(authRequest("password")))
                .isInstanceOf(InvalidTwoFactorCodeException.class);

        String secret = usersByEmail.get("john.doe@example.com").getTwoFactorSecret();
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
        assertThat(refreshed.getRefreshToken()).isNotEqualTo(auth.getRefreshToken());
        assertThat(refreshTokensByValue.get(auth.getRefreshToken()).isRevoked()).isTrue();
    }

    @Test
    void refreshTokenExpiredThrows() {
        authenticationService.register(registerRequest());
        AuthenticationResponse auth = authenticationService.authenticate(authRequest("password"));
        RefreshToken stored = refreshTokensByValue.get(auth.getRefreshToken());
        stored.setExpiryDate(Instant.now().minusSeconds(1));

        assertThatThrownBy(() -> authenticationService.refreshToken(auth.getRefreshToken()))
                .isInstanceOf(RefreshTokenInvalidException.class);
    }

    @Test
    void refreshTokenNotFoundThrows() {
        assertThatThrownBy(() -> authenticationService.refreshToken("missing"))
                .isInstanceOf(RefreshTokenInvalidException.class);
    }

    @Test
    void refreshTokenGeneratesNewTokenAndRevokesOld() {
        authenticationService.register(registerRequest());
        AuthenticationResponse initial = authenticationService.authenticate(authRequest("password"));

        RefreshToken original = refreshTokensByValue.get(initial.getRefreshToken());
        assertThat(original.isRevoked()).isFalse();

        AuthenticationResponse refreshed = authenticationService.refreshToken(initial.getRefreshToken());

        assertThat(refreshed.getToken()).isNotBlank();
        assertThat(refreshed.getRefreshToken()).isNotEqualTo(initial.getRefreshToken());
        assertThat(refreshTokensByValue.get(initial.getRefreshToken()).isRevoked()).isTrue();
        assertThat(refreshTokensByValue).containsKey(refreshed.getRefreshToken());
    }
}
