package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Config.CacheConfig;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.User.UserInfo;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.User.UserInfoRepository;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PlanoService.class, CacheConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PlanoServiceCacheTest {

    @Autowired
    private PlanoService planoService;

    @MockBean
    private PlanoRepository planoRepository;

    @MockBean
    private UserInfoRepository userInfoRepository;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void obterPlanoUsuarioIsCachedPerTenant() {
        User user = new User();
        Plano plano = new Plano();
        plano.setId(10);
        UserInfo userInfo = new UserInfo();
        userInfo.setPlanoAtivoId(plano);

        when(userInfoRepository.findByOwnerUser(user)).thenReturn(Optional.of(userInfo));
        when(planoRepository.findById(10)).thenReturn(Optional.of(plano));

        TenantContext.setTenantId(1);
        Optional<Plano> first = planoService.obterPlanoUsuario(user);
        Optional<Plano> second = planoService.obterPlanoUsuario(user);

        assertThat(first).contains(plano);
        assertThat(second).contains(plano);
        verify(planoRepository, times(1)).findById(10);
        verify(userInfoRepository, times(1)).findByOwnerUser(user);

        TenantContext.setTenantId(2);
        Optional<Plano> third = planoService.obterPlanoUsuario(user);

        assertThat(third).contains(plano);
        verify(planoRepository, times(2)).findById(10);
        verify(userInfoRepository, times(2)).findByOwnerUser(user);
    }
}

