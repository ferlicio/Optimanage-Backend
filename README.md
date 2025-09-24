# Optimanage-Backend

Backend do sistema Optimanage desenvolvido em Spring Boot para a gestão de produtos, serviços, clientes, fornecedores, vendas, compras e agenda.

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

## Execução
1. Requisitos: Java 17+ e Maven.
2. Rodar testes: `./mvnw test`
3. Executar aplicação: `./mvnw spring-boot:run`

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

