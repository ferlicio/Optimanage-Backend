package com.AIT.Optimanage.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Optimanage")
                        .version("1.0")
                        .description("Documentação da API do projeto Optimanage"));
    }
}
// para acessar: http://localhost:8080/swagger-ui/index.html


//exemplo para implementar nos endpoints:

//@RestController
//@RequestMapping("/api/v1/clientes")
//@Tag(name = "Clientes", description = "Operações relacionadas a clientes")
//public class VendaController extends BaseController {
//
//    @GetMapping
//    @Operation(summary = "Listar clientes", description = "Retorna uma lista de todos os clientes")
//    @ApiResponse(responseCode = "200", description = "Sucesso ao listar clientes")
//    public List<Cliente> listarClientes() {
//        // ...
//    }
//}