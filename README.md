# Optimanage-Backend

Backend do sistema Optimanage desenvolvido em Spring Boot para a gestão de produtos, serviços, clientes, fornecedores, vendas, compras e agenda.

O repositório concentra toda a API REST do Optimanage, responsável por orquestrar os fluxos de cadastro, relacionamento com clientes/fornecedores, pipeline de vendas e o módulo de inteligência que auxilia na tomada de decisão sobre estoque e recomendações. A seguir estão descritos os principais recursos, como executar o projeto localmente e detalhes de integração.

## Tecnologias principais

- **Java 17** com **Spring Boot 3** como base da aplicação.
- **Spring Security** com autenticação JWT e filtros de limitação de taxa.
- **Flyway** para versionamento das migrações de banco.
- **Maven Wrapper (`mvnw`)** para build e execução.
- **MariaDB/MySQL** (ou qualquer banco compatível com JDBC) como persistência.
- Observabilidade via **Spring Actuator** + **OpenTelemetry** com exportação OTLP.

## Estrutura do repositório

| Caminho | Descrição |
| --- | --- |
| `src/main/java` | Código-fonte principal (controllers, services, configs, domínios). |
| `src/main/resources` | Configurações (`application*.yml`) e templates Flyway. |
| `src/test/java` | Testes automatizados (unidade e integração). |
| `dashboard/` | Protótipo do dashboard e contratos JSON para o front-end. |
| `pipeline/` | Pipelines e scripts de automação (CI/CD). |

## Pré-requisitos

1. Java 17+ instalado e configurado no `JAVA_HOME`.
2. Docker opcional para subir serviços auxiliares (MariaDB, etc.).
3. Maven não é obrigatório, pois o wrapper `./mvnw` já é versionado.
4. Uma instância de banco de dados compatível com JDBC (a configuração padrão assume `optimanage` em `localhost:3307`).

### Variáveis de ambiente úteis

| Variável | Uso |
| --- | --- |
| `SPRING_DATASOURCE_URL` | Sobrescreve a URL do banco. |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Credenciais do banco. |
| `SPRING_PROFILES_ACTIVE` | Define os perfis ativos (`dev`, `test`, etc.). |
| `JWT_SECRET` | Segredo utilizado para assinar tokens. |
| `RATE_LIMITING_PROTECTED_PATTERNS` | Padrões de URL protegidos (pode sobrescrever o `application.yml`). |

Caso esteja usando Docker Compose, exporte essas variáveis antes de iniciar a aplicação.

## Funcionalidades
- Autenticação JWT para registro e login.
- Gerenciamento de produtos e serviços.
- Agenda de eventos.
- Controle de clientes e fornecedores com contatos e endereços.
- Registro de vendas e compras com fluxo de pagamento.
- Contextos e compatibilidades de vendas.

## Endpoints
Todos os recursos (exceto autenticação) usam o prefixo `/api/v1` e exigem um token JWT válido.

### Autenticação
- `POST /api/v1/auth/register` &ndash; registrar novo usuário.
- `POST /api/v1/auth/authenticate` &ndash; autenticar usuário.

### Usuários
- `POST /api/v1/usuarios/criar` &ndash; criar usuário (requer autoridade `ADMIN`).
- `GET /api/v1/usuarios/listar` &ndash; listar usuários (requer autoridade `ADMIN`).
- `GET /api/v1/usuarios/{id}` &ndash; obter usuário (requer autoridade `ADMIN`).
- `PUT /api/v1/usuarios/{id}/atualizar-plano?novoPlanoId={novoPlanoId}` &ndash; atualizar plano ativo (requer autoridade `ADMIN`).
- `DELETE /api/v1/usuarios/{id}/desativar` &ndash; desativar usuário (requer autoridade `ADMIN`).

### Produtos
- `GET /api/v1/produtos` &ndash; listar produtos.
- `GET /api/v1/produtos/{idProduto}` &ndash; obter um produto.
- `POST /api/v1/produtos` &ndash; criar produto.
- `PUT /api/v1/produtos/{idProduto}` &ndash; atualizar produto.
- `DELETE /api/v1/produtos/{idProduto}` &ndash; remover produto.
- Campos de controle de estoque: `estoqueMinimo` e `prazoReposicaoDias` permitem definir limites mínimos e o tempo médio de reposição para alimentar o monitoramento automático.

### Serviços
- `GET /api/v1/servicos`
- `GET /api/v1/servicos/{idServico}`
- `POST /api/v1/servicos`
- `PUT /api/v1/servicos/{idServico}`
- `DELETE /api/v1/servicos/{idServico}`

### Agenda
- `GET /api/v1/agenda` &ndash; listar eventos com filtros `data_inicial`, `data_final`, `sort`, `order`, `page`, `pagesize`.

### Clientes
- `GET /api/v1/clientes` &ndash; listar clientes (`id`, `nome`, `estado`, `cpfOuCnpj`, `atividade`, `tipoPessoa`, `ativo`, `sort`, `order`, `page`, `pagesize`).
- `GET /api/v1/clientes/{idCliente}` &ndash; obter cliente.
- `POST /api/v1/clientes` &ndash; criar cliente.
- `PUT /api/v1/clientes/{idCliente}` &ndash; atualizar cliente.
- `DELETE /api/v1/clientes/{idCliente}` &ndash; inativar cliente.
- `GET /api/v1/clientes/{idCliente}/contatos` &ndash; listar contatos.
- `POST /api/v1/clientes/{idCliente}/contatos` &ndash; adicionar contato.
- `PUT /api/v1/clientes/{idCliente}/contatos/{idContato}` &ndash; atualizar contato.
- `DELETE /api/v1/clientes/{idCliente}/contatos/{idContato}` &ndash; remover contato.
- `GET /api/v1/clientes/{idCliente}/enderecos` &ndash; listar endereços.
- `POST /api/v1/clientes/{idCliente}/enderecos` &ndash; adicionar endereço.
- `PUT /api/v1/clientes/{idCliente}/enderecos/{idEndereco}` &ndash; atualizar endereço.
- `DELETE /api/v1/clientes/{idCliente}/endereços/{idEndereco}` &ndash; remover endereço.

### Fornecedores
- `GET /api/v1/fornecedores` &ndash; listar fornecedores (`id`, `nome`, `cpfOuCnpj`, `atividade`, `estado`, `tipoPessoa`, `ativo`, `sort`, `order`, `page`, `pagesize`).
- `GET /api/v1/fornecedores/{idFornecedor}` &ndash; obter fornecedor.
- `POST /api/v1/fornecedores` &ndash; criar fornecedor.
- `PUT /api/v1/fornecedores/{idFornecedor}` &ndash; atualizar fornecedor.
- `DELETE /api/v1/fornecedores/{idFornecedor}` &ndash; inativar fornecedor.
- `GET /api/v1/fornecedores/{idFornecedor}/contatos` &ndash; listar contatos.
- `POST /api/v1/fornecedores/{idFornecedor}/contatos` &ndash; adicionar contato.
- `PUT /api/v1/fornecedores/{idFornecedor}/contatos/{idContato}` &ndash; atualizar contato.
- `DELETE /api/v1/fornecedores/{idFornecedor}/contatos/{idContato}` &ndash; remover contato.
- `GET /api/v1/fornecedor/{idFornecedor}/enderecos` &ndash; listar endereços.
- `POST /api/v1/fornecedor/{idFornecedor}/enderecos` &ndash; adicionar endereço.
- `PUT /api/v1/fornecedor/{idFornecedor}/enderecos/{idEndereco}` &ndash; atualizar endereço.
- `DELETE /api/v1/fornecedor/{idFornecedor}/enderecos/{idEndereco}` &ndash; remover endereço.

### Compras
- `GET /api/v1/compras` &ndash; listar compras (`id`, `fornecedor_id`, `data_inicial`, `data_final`, `pago`, `status`, `forma_pagamento`, `sort`, `order`, `page`, `pagesize`).
- `GET /api/v1/compras/{idCompra}` &ndash; obter compra.
- `POST /api/v1/compras` &ndash; criar compra.
- `PUT /api/v1/compras/{idCompra}` &ndash; editar compra.
- `PUT /api/v1/compras/{idCompra}/confirmar` &ndash; confirmar compra.
- `PUT /api/v1/compras/{idCompra}/pagar/{idPagamento}` &ndash; pagar compra.
- `PUT /api/v1/compras/{idCompra}/lancar-pagamento` &ndash; lançar pagamentos.
- `PUT /api/v1/compras/{idCompra}/estornar` &ndash; estornar compra.
- `PUT /api/v1/compras/{idCompra}/estornar/{idPagamento}` &ndash; estornar pagamento.
- `PUT /api/v1/compras/{idCompra}/agendar` &ndash; agendar compra.
- `PUT /api/v1/compras/{idCompra}/finalizar-agendamento` &ndash; finalizar agendamento.
- `PUT /api/v1/compras/{idCompra}/finalizar` &ndash; finalizar compra.
- `PUT /api/v1/compras/{idCompra}/cancelar` &ndash; cancelar compra.

> **Contrato da API de compras**
> - O campo `valorFinal` é calculado exclusivamente pelo servidor com base nos produtos e serviços enviados.
> - O payload de criação/edição não aceita mais `dataCobranca`; utilize os endpoints de pagamento para definir vencimentos.

### Vendas
- `GET /api/v1/vendas` &ndash; listar vendas (`id`, `cliente_id`, `data_inicial`, `data_final`, `pago`, `status`, `forma_pagamento`, `sort`, `order`, `page`, `pagesize`).
- `GET /api/v1/vendas/{idVenda}` &ndash; obter venda.
- `POST /api/v1/vendas` &ndash; registrar venda.
- `PUT /api/v1/vendas/{idVenda}` &ndash; editar venda.
- `PUT /api/v1/vendas/{idVenda}/confirmar` &ndash; confirmar venda.
- `PUT /api/v1/vendas/{idVenda}/pagar/{idPagamento}` &ndash; registrar pagamento.
- `PUT /api/v1/vendas/{idVenda}/lancar-pagamento` &ndash; lançar pagamentos.
- `PUT /api/v1/vendas/{idVenda}/estornar` &ndash; estornar venda.
- `PUT /api/v1/vendas/{idVenda}/estornar/{idPagamento}` &ndash; estornar pagamento.
- `PUT /api/v1/vendas/{idVenda}/agendar` &ndash; agendar venda.
- `PUT /api/v1/vendas/{idVenda}/finalizar-agendamento` &ndash; finalizar agendamento.
- `PUT /api/v1/vendas/{idVenda}/finalizar` &ndash; finalizar venda.
- `PUT /api/v1/vendas/{idVenda}/cancelar` &ndash; cancelar venda.

### Pagamentos
- `POST /api/v1/pagamentos/webhook` &ndash; receber eventos de provedores de pagamento.

### Contextos de Compatibilidade
- `GET /api/v1/contextos` &ndash; listar contextos.
- `GET /api/v1/contextos/{idContexto}` &ndash; obter contexto.
- `POST /api/v1/contextos` &ndash; criar contexto.
- `PUT /api/v1/contextos/{idContexto}` &ndash; atualizar contexto.
- `DELETE /api/v1/contextos/{idContexto}` &ndash; remover contexto.

### Compatibilidades
- `GET /api/v1/compatibilidades/{contexto}` &ndash; buscar compatibilidades.
- `POST /api/v1/compatibilidades` &ndash; adicionar compatibilidade.

### Recomendações
- `GET /api/v1/vendas/recomendacoes` &ndash; sugere produtos considerando apenas itens ativos, disponíveis para venda e (por padrão) com estoque positivo.
  - Disponível apenas quando o plano do usuário tiver `recomendacoesHabilitadas = true`.
  - Query params opcionais:
    - `clienteId`: filtra o cálculo para o histórico do cliente informado; quando omitido, utiliza a recorrência geral da organização.
    - `contexto`: nome do contexto de compatibilidade que concede bônus para produtos previamente marcados como compatíveis.
    - `estoquePositivo`: define se somente itens com estoque maior que zero devem ser retornados (padrão: `true`).
- Critérios de pontuação:
  - Coocorrência em vendas compartilhadas com o cliente ou na base global.
  - Recência das vendas (vendas mais recentes geram peso maior).
  - Recorrência agregada: itens recorrentes multiplicam a pontuação final.
  - Margem de contribuição (absoluta e percentual) prioriza itens mais rentáveis.
  - Bônus adicional para produtos compatíveis com o contexto informado.
- A lista final é limitada a dez sugestões ordenadas pela pontuação calculada.

### Analytics
- `GET /api/v1/analytics/resumo` &ndash; resumo de vendas, compras e lucro.
- `GET /api/v1/analytics/previsao` &ndash; previsão de demanda com regressão linear.
- `GET /api/v1/analytics/estoque-critico` &ndash; lista itens com estoque crítico ou em risco de ruptura, incluindo projeção de dias restantes e sugestão de compra.
  - Disponível apenas para organizações cujo plano tenha `monitoramentoEstoqueHabilitado = true`.

### Monitoramento de estoque
- Um job agendado diário (padrão `0 0 6 * * *`, configurável via `inventory.monitoring.cron`) reprocessa o consumo médio dos últimos 30 dias a partir do `InventoryHistory`.
- Para cada produto ativo, o serviço calcula os dias restantes considerando o estoque atual, o consumo médio e o prazo de reposição configurado.
- Alertas críticos ou de atenção são persistidos na tabela `inventory_alert` e expostos pelo endpoint de analytics quando o plano atual possuir a permissão de monitoramento de estoque.

## Como executar

1. Instale os pré-requisitos listados acima.
2. Copie o arquivo de configuração padrão se precisar customizar localmente: `cp src/main/resources/application.yml src/main/resources/application-local.yml` e ajuste as propriedades.
3. Configure a base de dados (ex.: crie o schema `optimanage`).
4. Rode as migrações e testes:
   ```bash
   ./mvnw flyway:migrate
   ./mvnw test
   ```
5. Execute a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

> **Dica:** utilize `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` para habilitar ferramentas adicionais de debug, collection do Postman automática e logs mais verbosos.

### Perfil de desenvolvimento
Para habilitar o perfil `dev` e gerar a coleção Postman automaticamente na inicialização, execute a aplicação com o perfil de desenvolvimento:

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Ou defina a variável de ambiente `SPRING_PROFILES_ACTIVE=dev` ao executar o JAR.

O arquivo `src/main/resources/application-dev.properties` ativa esse perfil.

## Migrações de Banco de Dados
As migrações de esquema são gerenciadas pelo [Flyway](https://flywaydb.org/). Os scripts SQL ficam em `src/main/resources/db/migration` e são aplicados automaticamente na inicialização da aplicação.

Para executar as migrações manualmente, utilize o Maven especificando a conexão com o banco:

```
./mvnw flyway:migrate \
    -Dflyway.url=jdbc:mariadb://localhost:3307/optimanage \
    -Dflyway.user=<usuario> \
    -Dflyway.password=<senha>
```

## Limitação de taxa
A lista de endpoints protegidos pelo `RateLimitingFilter` é configurada pela propriedade
`rate-limiting.protected-patterns` no `application.yml`. Ela aceita padrões de URL no formato Ant.

Exemplo para proteger os endpoints de redefinição de senha e criação de conta:

```yaml
rate-limiting:
  protected-patterns:
    - /auth/reset-password
    - /auth/register
```

Com essa configuração, as rotas de redefinição de senha e criação de conta ficam sujeitas ao controle de limite de requisições.

## Monitoramento
- `GET /actuator/health` – verificar status da aplicação.
- `GET /actuator/info` – informações adicionais incluindo contagem de clientes e produtos.
- `GET /actuator/metrics` – métricas do sistema e da JVM.
- `GET /actuator/prometheus` – métricas no formato Prometheus.
- Traces são exportados via OpenTelemetry OTLP para `http://localhost:4317` por padrão.
- Contadores de autenticação:
  - `auth.register.success` e `auth.register.failure` – registros bem-sucedidos e falhos.
  - `auth.authenticate.success` e `auth.authenticate.failure` – logins bem-sucedidos e falhos.

## Testes e qualidade

- Testes unitários e de integração: `./mvnw test`.
- Checagem de formatação (Spotless/Checkstyle, se configurado no `pom.xml`): `./mvnw spotless:apply` / `./mvnw checkstyle:check`.
- Para cenários de carga utilize ferramentas externas (ex.: k6 ou JMeter) apontando para os endpoints documentados acima.

## Deploy e CI/CD

- O diretório `pipeline/` contém exemplos de scripts para integração contínua (GitHub Actions/GitLab CI). Ajuste as variáveis de ambiente conforme a infraestrutura utilizada.
- Utilize `./mvnw package -DskipTests` para gerar o JAR final antes de publicar.
- Para empacotar a aplicação em contêiner, crie uma imagem baseada em `eclipse-temurin:17-jre` e copie o arquivo gerado em `target/optimanage-*.jar`.

## Suporte

Em caso de dúvidas ou sugestões, abra uma issue descrevendo o problema, logs relevantes e como reproduzir. Pull requests são bem-vindos e devem incluir testes cobrindo a alteração proposta.

