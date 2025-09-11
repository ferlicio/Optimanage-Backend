package com.AIT.Optimanage.Auth;

import com.AIT.Optimanage.Config.JwtService;
import com.AIT.Optimanage.Config.AuthProperties;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Auth.TokenBlacklistService;
import com.AIT.Optimanage.Support.EmailService;
import com.AIT.Optimanage.Exceptions.UserNotFoundException;
import com.AIT.Optimanage.Exceptions.InvalidTwoFactorCodeException;
import com.AIT.Optimanage.Exceptions.InvalidResetCodeException;
import com.AIT.Optimanage.Exceptions.RefreshTokenNotFoundException;
import com.AIT.Optimanage.Exceptions.RefreshTokenInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailService emailService;
    private final AuthProperties authProperties;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .nome(request.getNome())
                .sobrenome(request.getSobrenome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(Role.USER)
                .ativo(true)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(
                java.util.Map.<String, Object>of("tenantId", user.getTenantId()),
                user
        );
        var refreshToken = createRefreshToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var optionalUser = userRepository.findByEmail(request.getEmail());
        var user = optionalUser.orElse(null);
        if (user != null && user.getLockoutExpiry() != null && user.getLockoutExpiry().isAfter(Instant.now())) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            userRepository.save(user);
            log.warn("Login attempt for locked user: {}", request.getEmail());
            throw new LockedException("User account is locked");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getSenha()
                    )
            );
        } catch (AuthenticationException ex) {
            if (ex instanceof BadCredentialsException && user != null) {
                int attempts = user.getFailedAttempts() + 1;
                user.setFailedAttempts(attempts);
                if (attempts >= authProperties.getMaxFailedAttempts()) {
                    user.setLockoutExpiry(Instant.now().plus(Duration.ofMinutes(authProperties.getLockoutMinutes())));
                }
                userRepository.save(user);
            }
            throw ex;
        }
        if (user == null) {
            throw new UserNotFoundException();
        }
        user.setFailedAttempts(0);
        user.setLockoutExpiry(null);
        userRepository.save(user);
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            String secret = user.getTwoFactorSecret();
            if (secret != null) {
                String code = request.getTwoFactorCode();
                if (code == null) {
                    throw new InvalidTwoFactorCodeException();
                }
                int codeInt;
                try {
                    codeInt = Integer.parseInt(code);
                } catch (NumberFormatException e) {
                    throw new InvalidTwoFactorCodeException();
                }
                GoogleAuthenticator gAuth = new GoogleAuthenticator();
                if (!gAuth.authorize(secret, codeInt)) {
                    throw new InvalidTwoFactorCodeException();
                }
            }
        }
        var jwtToken = jwtService.generateToken(
                java.util.Map.<String, Object>of("tenantId", user.getTenantId()),
                user
        );
        var refreshToken = createRefreshToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TwoFactorSetupResponse toggleTwoFactor(TwoFactorToggleRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);
        if (request.isEnable()) {
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            GoogleAuthenticatorKey key = gAuth.createCredentials();
            user.setTwoFactorSecret(key.getKey());
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthURL("Optimanage", user.getEmail(), key);
            String qrUrl = "https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=" +
                    URLEncoder.encode(otpAuthURL, StandardCharsets.UTF_8);
            return new TwoFactorSetupResponse(qrUrl);
        } else {
            user.setTwoFactorEnabled(false);
            user.setTwoFactorSecret(null);
            userRepository.save(user);
            return new TwoFactorSetupResponse(null);
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);
        sendResetCode(user);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);
        if (user.getResetCode() == null || !user.getResetCode().equals(request.getCode())
                || user.getResetCodeExpiry() == null || user.getResetCodeExpiry().isBefore(Instant.now())) {
            throw new InvalidResetCodeException();
        }
        user.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public AuthenticationResponse refreshToken(String token) {
        if (refreshTokenRepository.revokeIfNotRevoked(token) == 0) {
            throw new RefreshTokenInvalidException();
        }
        var storedToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(RefreshTokenNotFoundException::new);
        var user = storedToken.getUser();
        if (storedToken.getExpiryDate().isBefore(Instant.now()) || !jwtService.isTokenValid(token, user)) {
            throw new RefreshTokenInvalidException();
        }
        var newRefreshToken = jwtService.generateRefreshToken(user);
        var newTokenEntity = RefreshToken.builder()
                .user(user)
                .token(newRefreshToken)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshExpiration()))
                .build();
        refreshTokenRepository.save(newTokenEntity);
        var jwtToken = jwtService.generateToken(
                java.util.Map.<String, Object>of("tenantId", user.getTenantId()),
                user
        );
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    private String createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        var token = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshExpiration()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(token);
        return refreshToken;
    }

    @Transactional
    public void logout(String token) {
        var userEmail = jwtService.extractEmail(token);
        if (userEmail != null) {
            userRepository.findByEmail(userEmail)
                    .ifPresent(refreshTokenRepository::deleteByUser);
        }
        tokenBlacklistService.blacklistToken(token);
    }

    private void sendResetCode(User user) {
        String code = generateCode();
        user.setResetCode(code);
        user.setResetCodeExpiry(Instant.now().plusSeconds(authProperties.getResetCodeExpirySeconds()));
        userRepository.save(user);
        sendCodeAsync(user.getEmail(), code);
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private void sendCodeAsync(String destination, String code) {
        emailService.enviarCodigo(destination, code);
    }
}
