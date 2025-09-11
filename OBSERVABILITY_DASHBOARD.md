# Observability Dashboard

## Authentication Metrics

| Métrica | Descrição |
| --- | --- |
| `auth.register.success` | Quantidade de registros de usuários bem-sucedidos |
| `auth.register.failure` | Registros de usuários que falharam |
| `auth.authenticate.success` | Autenticações realizadas com sucesso |
| `auth.authenticate.failure` | Tentativas de autenticação que falharam |

Essas métricas podem ser consultadas via `/actuator/metrics`.
