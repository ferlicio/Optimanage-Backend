package com.AIT.Optimanage.Controllers.Organization;

import com.AIT.Optimanage.Auth.AuthenticationResponse;
import com.AIT.Optimanage.Controllers.BaseController.V1BaseController;
import com.AIT.Optimanage.Controllers.User.dto.UserRequest;
import com.AIT.Optimanage.Controllers.dto.OrganizationRequest;
import com.AIT.Optimanage.Controllers.dto.OrganizationResponse;
import com.AIT.Optimanage.Controllers.dto.UserInviteRequest;
import com.AIT.Optimanage.Controllers.dto.UserInviteResponse;
import com.AIT.Optimanage.Services.Organization.OrganizationService;
import com.AIT.Optimanage.Services.Organization.UserInviteService;
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
    private final UserInviteService userInviteService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    @Operation(summary = "Criar organização", description = "Cria uma nova organização e usuário OWNER")
    @ApiResponse(responseCode = "201", description = "Organização criada")
    public ResponseEntity<OrganizationResponse> criar(@RequestBody @Valid OrganizationRequest request,
                                                      @org.springframework.security.core.annotation.AuthenticationPrincipal
                                                      com.AIT.Optimanage.Models.User.User loggedUser) {
        OrganizationResponse response = organizationService.criarOrganizacao(request, loggedUser);
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

    @PostMapping("/{id}/invites")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    @Operation(summary = "Gerar convite", description = "Gera um código de convite para novos usuários")
    @ApiResponse(responseCode = "201", description = "Convite criado")
    public ResponseEntity<UserInviteResponse> gerarConvite(@PathVariable Integer id,
                                                           @RequestBody @Valid UserInviteRequest request,
                                                           @org.springframework.security.core.annotation.AuthenticationPrincipal com.AIT.Optimanage.Models.User.User loggedUser) {
        UserInviteResponse response = userInviteService.gerarConvite(id, request, loggedUser.getId());
        return created(response);
    }

    @DeleteMapping("/{id}/invites/{code}")
    @PreAuthorize("hasAnyAuthority('OWNER','ADMIN')")
    @Operation(summary = "Revogar convite", description = "Revoga um código de convite")
    @ApiResponse(responseCode = "204", description = "Convite revogado")
    public ResponseEntity<Void> revogarConvite(@PathVariable Integer id, @PathVariable String code) {
        userInviteService.revogarConvite(id, code);
        return noContent();
    }
}
