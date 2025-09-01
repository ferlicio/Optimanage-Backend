package com.AIT.Optimanage.Auth;

import com.AIT.Optimanage.Config.JwtService;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Auth.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

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
        var jwtToken = jwtService.generateToken(java.util.Map.of("tenantId", user.getTenantId()), user);
        var refreshToken = createRefreshToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getSenha()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            sendTwoFactorCode(user);
            return AuthenticationResponse.builder()
                    .twoFactorRequired(true)
                    .build();
        }
        var jwtToken = jwtService.generateToken(java.util.Map.of("tenantId", user.getTenantId()), user);
        var refreshToken = createRefreshToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public AuthenticationResponse verifyTwoFactor(TwoFactorRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getTwoFactorCode() == null || !user.getTwoFactorCode().equals(request.getCode())
                || user.getTwoFactorExpiry() == null || user.getTwoFactorExpiry().isBefore(Instant.now())) {
            throw new RuntimeException("Invalid 2FA code");
        }
        user.setTwoFactorCode(null);
        user.setTwoFactorExpiry(null);
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(java.util.Map.of("tenantId", user.getTenantId()), user);
        var refreshToken = createRefreshToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void toggleTwoFactor(TwoFactorToggleRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(request.isEnable());
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        sendResetCode(user);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getResetCode() == null || !user.getResetCode().equals(request.getCode())
                || user.getResetCodeExpiry() == null || user.getResetCodeExpiry().isBefore(Instant.now())) {
            throw new RuntimeException("Invalid reset code");
        }
        user.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public AuthenticationResponse refreshToken(String token) {
        var storedToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        var user = storedToken.getUser();
        if (storedToken.getExpiryDate().isBefore(Instant.now()) || !jwtService.isTokenValid(token, user)) {
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("Refresh token invalid");
        }
        var jwtToken = jwtService.generateToken(java.util.Map.of("tenantId", user.getTenantId()), user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(token)
                .build();
    }

    private String createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        var token = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshExpiration()))
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

    private void sendTwoFactorCode(User user) {
        String code = generateCode();
        user.setTwoFactorCode(code);
        user.setTwoFactorExpiry(Instant.now().plusSeconds(300));
        userRepository.save(user);
        sendCode(user.getEmail(), code);
    }

    private void sendResetCode(User user) {
        String code = generateCode();
        user.setResetCode(code);
        user.setResetCodeExpiry(Instant.now().plusSeconds(600));
        userRepository.save(user);
        sendCode(user.getEmail(), code);
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private void sendCode(String destination, String code) {
        System.out.println("Sending code " + code + " to " + destination);
    }
}
