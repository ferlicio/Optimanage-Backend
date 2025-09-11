package com.AIT.Optimanage.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Schema<?> problemDetailSchema = new Schema<>()
                .addProperty("type", new StringSchema().example("https://api.optimanage.com/problems/example"))
                .addProperty("title", new StringSchema().example("Human readable title"))
                .addProperty("status", new IntegerSchema().example(400))
                .addProperty("detail", new StringSchema().example("More information about the error"))
                .addProperty("correlationId", new StringSchema().example("123e4567-e89b-12d3-a456-426614174000"))
                .addProperty("errors", new ArraySchema().items(new StringSchema()).example(List.of("field: must not be null")));

        return new OpenAPI()
                .components(new Components().addSchemas("ProblemDetail", problemDetailSchema))
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