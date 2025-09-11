package com.AIT.Optimanage.Auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de autenticação")
public class AuthenticationController extends com.AIT.Optimanage.Controllers.BaseController.V1BaseController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Registrar", description = "Registra um novo usuário")
    @ApiResponse(responseCode = "201", description = "Criado")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return created(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Autenticar", description = "Autentica um usuário")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request) {
        return ok(authenticationService.authenticate(request));
    }

    @PostMapping("/toggle-2fa")
    @Operation(summary = "Ativar/Desativar 2FA", description = "Ativa ou desativa o 2FA para o usuário")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<TwoFactorSetupResponse> toggleTwoFactor(
            @Valid @RequestBody TwoFactorToggleRequest request) {
        return ok(authenticationService.toggleTwoFactor(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Esqueci a senha", description = "Envia código para redefinição de senha")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha", description = "Redefine a senha usando código")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Atualizar token", description = "Atualiza o token JWT")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<AuthenticationResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ok(authenticationService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoga tokens do usuário")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            log.warn("Logout attempt without Authorization header");
            return ResponseEntity.badRequest().build();
        }
        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Logout attempt with malformed Authorization header: {}", authHeader);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        authenticationService.logout(token);
        return ResponseEntity.ok().build();
    }
}
