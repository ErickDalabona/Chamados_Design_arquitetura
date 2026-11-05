# Sistema de Chamados

API REST em Java 21 e Spring Boot (arquitetura em camadas), com H2 em memória.

## Como rodar

    .\mvnw spring-boot:run

A API sobe em http://localhost:8080 e o console do H2 em /h2-console.

Documentação da API (Swagger): http://localhost:8080/swagger-ui.html

## Rotas

| Recurso | Rotas |
|---|---|
| Clientes | POST, GET, GET/{id}, PUT/{id}, DELETE/{id} em /clientes |
| Categorias | POST, GET, GET/{id}, PUT/{id}, DELETE/{id} em /categorias |
| Técnicos | POST, GET, GET/{id}, PUT/{id}, DELETE/{id} em /tecnicos |
| Chamados | POST, GET, GET/{id}, PUT/{id}, DELETE/{id} em /chamados |
| Atribuir técnico | PATCH /chamados/{id}/atribuir |
| Alterar status | PATCH /chamados/{id}/status |
| Chamados de um cliente | GET /clientes/{id}/chamados |
| Comentários | POST e GET em /chamados/{id}/comentarios |

## Exemplos de consulta de chamados

    GET /chamados
    GET /chamados?status=ABERTO
    GET /chamados?prioridade=ALTA
    GET /chamados?clienteId=1
    GET /chamados?status=ABERTO&prioridade=ALTA&clienteId=1
    GET /chamados?page=0&size=5
    GET /chamados?sort=prioridade,asc
    GET /clientes/1/chamados

Status: ABERTO, EM_ATENDIMENTO, AGUARDANDO_CLIENTE, FECHADO.
Prioridades: BAIXA, MEDIA, ALTA, CRITICA.

## Regras

- O chamado nasce ABERTO, com a data de abertura preenchida automaticamente.
- Chamado FECHADO não pode ser alterado nem receber técnico.
- Ao fechar, a data de fechamento é preenchida automaticamente.
- Comentários registram texto, autor e data (automática) em um chamado.