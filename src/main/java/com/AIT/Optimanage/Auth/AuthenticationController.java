package com.AIT.Optimanage.Auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authenticationService.logout(token);
        }
        return ResponseEntity.ok().build();
    }
}
