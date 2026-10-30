# Comanda — API de gerenciamento de comandas e estoque

Este projeto é uma API REST para gerenciar comandas e estoque de bares e restaurantes. Ele permite cadastrar estabelecimentos, usuários, clientes, mesas, grupos e produtos, além de acompanhar o consumo de uma comanda desde a abertura até o fechamento ou cancelamento.

## Objetivo do projeto

Meu objetivo principal foi aprender, na prática, a conectar uma aplicação a um banco de dados PostgreSQL e executar o backend e o banco utilizando Docker.

Queria entender o caminho completo: enviar uma requisição, processar os dados na aplicação, gravá-los no banco e consultar o resultado. Também aprender a configurar variáveis de ambiente, conectar containers e manter os dados salvos mesmo depois de recriar os containers.

A escrita do código não foi o foco principal, embora tenha proporcionado um grande aprendizado sobre estrutura de projetos, separação de responsabilidades e regras de negócio.

## Desenvolvimento e aprendizado

Utilizei inteligência artificial como apoio para escrever e organizar o código, tirar dúvidas e realizar testes. O projeto foi desenvolvido dentro de um nível que consigo compreender, acompanhando o funcionamento das partes e buscando entender as decisões tomadas.

Durante esse processo, aprendi sobre a divisão entre controllers, services, repositories e DTOs: receber requisições, aplicar regras de negócio, acessar o banco e definir quais dados entram e saem da API.

Também trabalhei com validação de dados, tratamento de erros, relacionamentos entre tabelas e controle de alterações na estrutura do banco com Flyway.

## Funcionalidades

- Cadastro e atualização de estabelecimentos, usuários, clientes, mesas, grupos e produtos.
- Abertura, consulta, fechamento e cancelamento de comandas.
- Inclusão e remoção de itens, com cálculo do total.
- Redução do estoque ao adicionar produtos à comanda.
- Devolução do estoque ao remover itens ou cancelar uma comanda.
- Ajustes manuais de estoque.
- Filtros de comandas por estabelecimento e status.

A aplicação também aplica regras como impedir duas comandas abertas na mesma mesa, bloquear alterações em comandas encerradas e recusar operações que deixariam o estoque negativo.

## Tecnologias utilizadas

- Java 25 e Spring Boot.
- Spring MVC e Spring Data JPA.
- PostgreSQL e Flyway.
- Maven e Lombok.
- Docker e Docker Compose.
- Swagger/OpenAPI.
- JUnit, Mockito e Testcontainers.

## Testes realizados

Foram realizados testes pelo Swagger com dados fictícios, incluindo cadastros, abertura de comandas, inclusão e remoção de itens, cálculo de valores, fechamento, cancelamento e movimentações de estoque.

Também foram verificadas respostas para situações inválidas, como estoque insuficiente, quantidade zero, registros inexistentes e tentativa de alterar uma comanda encerrada. Esses testes cobrem os fluxos exercitados, sem representar uma validação completa de todos os cenários do sistema.

As consultas SQL no PostgreSQL permitem acompanhar os registros e entender como as operações da API se relacionam com as tabelas.

## Resultado

O projeto reúne os principais conhecimentos que eu queria praticar: integração entre Java e PostgreSQL, persistência de dados e execução com Docker Compose.

Além de construir uma API, o aprendizado foi entender como suas partes se conectam e assumir responsabilidade pelo código que utilizo, mesmo contando com o apoio de inteligência artificial durante o desenvolvimento
