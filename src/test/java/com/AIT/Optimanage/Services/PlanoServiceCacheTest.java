package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Config.CacheConfig;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Repositories.ProdutoRepository;
import com.AIT.Optimanage.Repositories.Cliente.ClienteRepository;
import com.AIT.Optimanage.Repositories.Fornecedor.FornecedorRepository;
import com.AIT.Optimanage.Repositories.ServicoRepository;
import com.AIT.Optimanage.Mappers.PlanoMapper;
import com.AIT.Optimanage.Services.AuditTrailService;
import com.AIT.Optimanage.Services.User.UsuarioService;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PlanoService.class, UsuarioService.class, CacheConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PlanoServiceCacheTest {

    @Autowired
    private PlanoService planoService;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private PlanoRepository planoRepository;

    @MockBean
    private OrganizationRepository organizationRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private ProdutoRepository produtoRepository;

    @MockBean
    private ClienteRepository clienteRepository;

    @MockBean
    private FornecedorRepository fornecedorRepository;

    @MockBean
    private ServicoRepository servicoRepository;

    @MockBean
    private PlanoMapper planoMapper;

    @MockBean
    private AuditTrailService auditTrailService;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void obterPlanoUsuarioIsCachedPerOrganization() {
        User owner = new User();
        owner.setId(1);
        owner.setTenantId(1);
        User collaborator = new User();
        collaborator.setId(2);
        collaborator.setTenantId(1);
        Plano plano = new Plano();
        plano.setId(10);
        Organization organization = new Organization();
        organization.setPlanoAtivoId(plano);

        when(organizationRepository.findById(1)).thenReturn(Optional.of(organization));
        when(planoRepository.findById(10)).thenReturn(Optional.of(plano));

        Optional<Plano> first = planoService.obterPlanoUsuario(owner);
        Optional<Plano> second = planoService.obterPlanoUsuario(collaborator);

        assertThat(first).contains(plano);
        assertThat(second).contains(plano);
        verify(planoRepository, times(1)).findById(10);
        verify(organizationRepository, times(1)).findById(1);

        User otherTenantUser = new User();
        otherTenantUser.setId(3);
        otherTenantUser.setTenantId(2);
        Plano otherPlano = new Plano();
        otherPlano.setId(20);
        Organization otherOrganization = new Organization();
        otherOrganization.setPlanoAtivoId(otherPlano);

        when(organizationRepository.findById(2)).thenReturn(Optional.of(otherOrganization));
        when(planoRepository.findById(20)).thenReturn(Optional.of(otherPlano));

        Optional<Plano> third = planoService.obterPlanoUsuario(otherTenantUser);

        assertThat(third).contains(otherPlano);
        verify(organizationRepository, times(1)).findById(2);
        verify(planoRepository, times(1)).findById(20);
    }

    @Test
    void cacheEvictedWhenUserPlanChanges() {
        User user = new User();
        user.setId(1);
        user.setTenantId(1);
        user.setOrganizationId(1);
        Plano antigo = new Plano();
        antigo.setId(10);
        Plano novo = new Plano();
        novo.setId(20);
        Organization info = new Organization();
        info.setId(1);
        info.setPlanoAtivoId(antigo);
        info.setOwnerUser(user);
        info.setTenantId(1);

        when(userRepository.findByIdAndOrganizationId(1, 1)).thenReturn(Optional.of(user));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(organizationRepository.findById(1)).thenReturn(Optional.of(info));
        when(planoRepository.findById(10)).thenReturn(Optional.of(antigo));
        when(planoRepository.findById(20)).thenReturn(Optional.of(novo));
        when(organizationRepository.save(info)).thenReturn(info);

        TenantContext.setTenantId(1);

        Optional<Plano> first = planoService.obterPlanoUsuario(user);
        assertThat(first).contains(antigo);
        verify(organizationRepository, times(1)).findById(1);
        verify(planoRepository, times(1)).findById(10);

        usuarioService.atualizarPlanoAtivo(1, 20);

        Optional<Plano> second = planoService.obterPlanoUsuario(user);
        assertThat(second).contains(novo);
        verify(organizationRepository, times(3)).findById(1);
        verify(organizationRepository, times(1)).save(info);
        verify(planoRepository, times(2)).findById(10);
        verify(planoRepository, times(2)).findById(20);
    }
}

