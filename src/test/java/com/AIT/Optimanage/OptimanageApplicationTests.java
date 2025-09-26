package com.AIT.Optimanage;

import com.AIT.Optimanage.Support.PlatformDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("test")
class OptimanageApplicationTests {

        @MockBean
        private PlatformDataInitializer platformDataInitializer;

        @Test
        void contextLoads() {
        }

}
