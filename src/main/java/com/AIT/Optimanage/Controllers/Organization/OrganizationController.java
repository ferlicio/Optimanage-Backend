package com.AIT.Optimanage.Controllers.Organization;

import com.AIT.Optimanage.Auth.AuthenticationResponse;
import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.User.dto.UserRequest;
import com.AIT.Optimanage.Controllers.dto.OrganizationRequest;
import com.AIT.Optimanage.Controllers.dto.OrganizationResponse;
import com.AIT.Optimanage.Services.Organization.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizações", description = "Gerenciamento de organizações")
public class OrganizationController extends V1BaseController {

    private final OrganizationService organizationService;

    @PostMapping
    @Operation(summary = "Criar organização", description = "Cria uma nova organização e usuário OWNER")
    @ApiResponse(responseCode = "201", description = "Organização criada")
    public ResponseEntity<OrganizationResponse> criar(@RequestBody @Valid OrganizationRequest request) {
        OrganizationResponse response = organizationService.criarOrganizacao(request);
        return created(response);
    }

    @PostMapping("/{id}/users")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    @Operation(summary = "Adicionar usuário", description = "Adiciona um usuário à organização")
    @ApiResponse(responseCode = "201", description = "Usuário criado")
    public ResponseEntity<AuthenticationResponse> adicionarUsuario(@PathVariable Integer id,
                                                                   @RequestBody @Valid UserRequest request) {
        AuthenticationResponse response = organizationService.adicionarUsuario(id, request);
        return created(response);
    }
}
