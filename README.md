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

### Produtos
- `GET /api/v1/produtos` &ndash; listar produtos.
- `GET /api/v1/produtos/{idProduto}` &ndash; obter um produto.
- `POST /api/v1/produtos` &ndash; criar produto.
- `PUT /api/v1/produtos/{idProduto}` &ndash; atualizar produto.
- `DELETE /api/v1/produtos/{idProduto}` &ndash; remover produto.

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
- `PUT /api/v1/compras/{idCompra}/finalizar` &ndash; finalizar compra.
- `PUT /api/v1/compras/{idCompra}/cancelar` &ndash; cancelar compra.

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

### Contextos de Compatibilidade
- `GET /api/v1/contextos` &ndash; listar contextos.
- `GET /api/v1/contextos/{idContexto}` &ndash; obter contexto.
- `POST /api/v1/contextos` &ndash; criar contexto.
- `PUT /api/v1/contextos/{idContexto}` &ndash; atualizar contexto.
- `DELETE /api/v1/contextos/{idContexto}` &ndash; remover contexto.

### Compatibilidades
- `GET /api/v1/compatibilidades/{contexto}` &ndash; buscar compatibilidades.
- `POST /api/v1/compatibilidades` &ndash; adicionar compatibilidade.

## Execução
1. Requisitos: Java 17+ e Maven.
2. Rodar testes: `./mvnw test`
3. Executar aplicação: `./mvnw spring-boot:run`

## Monitoramento
- `GET /actuator/health` – verificar status da aplicação.
- `GET /actuator/info` – informações adicionais incluindo contagem de clientes e produtos.

