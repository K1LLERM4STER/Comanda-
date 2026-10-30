package com.comanda.comanda;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.comanda.comanda.Model.StatusComanda;
import com.comanda.comanda.dto.request.ItemRequest;
import com.comanda.comanda.exception.RegraNegocioException;
import com.comanda.comanda.service.ComandaService;

@Testcontainers
@SpringBootTest(properties = {
        "spring.config.import=",
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ComandaConcorrenciaTests {

    @Container
    // A extensao JUnit do Testcontainers inicia e encerra este recurso.
    @SuppressWarnings("resource")
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("comanda_concorrencia_test");

    @DynamicPropertySource
    static void configurarBancoTemporario(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    ComandaService service;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PlatformTransactionManager transactionManager;

    long comandaId;
    long produtoId;
    long itemId;

    @BeforeEach
    void prepararDadosNoContainer() throws Exception {
        // Protecao adicional antes de limpar dados: apenas o banco deste container.
        try (var connection = jdbc.getDataSource().getConnection()) {
            assertThat(connection.getMetaData().getURL()).isEqualTo(postgres.getJdbcUrl());
            assertThat(connection.getCatalog()).isEqualTo("comanda_concorrencia_test");
        }
        jdbc.execute("TRUNCATE item_comanda, comanda, produto, grupo, mesa, usuario, cliente, "
                + "estabelecimento RESTART IDENTITY CASCADE");
        long estabelecimentoId = inserir("""
                INSERT INTO estabelecimento (nome, cnpj, endereco, email, ativo)
                VALUES ('Restaurante teste', '11222333000181', 'Rua teste', 'restaurante@example.com', true)
                RETURNING id
                """);
        long usuarioId = inserir("""
                INSERT INTO usuario (nome, email, senha, estabelecimento_id)
                VALUES ('Usuario teste', 'usuario@example.com', 'hash-ficticio-de-teste', ?) RETURNING id
                """, estabelecimentoId);
        long mesaId = inserir("""
                INSERT INTO mesa (numero_mesa, estabelecimento_id) VALUES ('1', ?) RETURNING id
                """, estabelecimentoId);
        long grupoId = inserir("""
                INSERT INTO grupo (descricao, estabelecimento_id) VALUES ('Bebidas', ?) RETURNING id
                """, estabelecimentoId);
        produtoId = inserir("""
                INSERT INTO produto (grupo_id, estabelecimento_id, unidade, descricao, estoque, pvenda)
                VALUES (?, ?, 'UN', 'Agua', 8, 5.00) RETURNING id
                """, grupoId, estabelecimentoId);
        comandaId = inserir("""
                INSERT INTO comanda (usuario_id, estabelecimento_id, mesa_id, status, data_abertura)
                VALUES (?, ?, ?, 'ABERTA', CURRENT_TIMESTAMP) RETURNING id
                """, usuarioId, estabelecimentoId, mesaId);
        itemId = inserir("""
                INSERT INTO item_comanda (comanda_id, produto_id, quantidade, valor_unitario)
                VALUES (?, ?, 2, 5.00) RETURNING id
                """, comandaId, produtoId);
    }

    static Stream<Arguments> disputas() {
        // Estoque inicial 8 + 2 unidades consumidas = 10 unidades no total.
        return Stream.of(
                Arguments.of(Operacao.CANCELAR, Operacao.CANCELAR, false, StatusComanda.CANCELADA, 10L, 2L),
                Arguments.of(Operacao.ADICIONAR, Operacao.FECHAR, true, StatusComanda.FECHADA, 7L, 3L),
                Arguments.of(Operacao.FECHAR, Operacao.ADICIONAR, false, StatusComanda.FECHADA, 8L, 2L),
                Arguments.of(Operacao.REMOVER, Operacao.FECHAR, true, StatusComanda.FECHADA, 10L, 0L),
                Arguments.of(Operacao.FECHAR, Operacao.REMOVER, false, StatusComanda.FECHADA, 8L, 2L),
                Arguments.of(Operacao.REMOVER, Operacao.CANCELAR, true, StatusComanda.CANCELADA, 10L, 0L),
                Arguments.of(Operacao.CANCELAR, Operacao.REMOVER, false, StatusComanda.CANCELADA, 10L, 2L));
    }

    @Test
    void cancelarDeveDevolverTodosOsItensDoMesmoProdutoComPrecosDiferentes() {
        jdbc.update("UPDATE produto SET estoque = 5 WHERE id = ?", produtoId);
        inserir("""
                INSERT INTO item_comanda (comanda_id, produto_id, quantidade, valor_unitario)
                VALUES (?, ?, 3, 6.00) RETURNING id
                """, comandaId, produtoId);

        var resposta = service.cancelarComanda(comandaId);

        assertThat(resposta.status()).isEqualTo(StatusComanda.CANCELADA);
        assertThat(resposta.itens()).hasSize(2);
        assertThat(jdbc.queryForObject("SELECT estoque FROM produto WHERE id = ?", Long.class, produtoId))
                .isEqualTo(10L);
    }

    @ParameterizedTest(name = "{0} antes de {1}")
    @MethodSource("disputas")
    void deveSerializarOperacoesNaMesmaComanda(Operacao primeira, Operacao segunda,
            boolean segundaPermitida, StatusComanda statusFinal, long estoqueFinal, long quantidadeFinal)
            throws Exception {
        var primeiraAplicada = new CountDownLatch(1);
        var liberarPrimeira = new CountDownLatch(1);
        var pidSegunda = new CompletableFuture<Integer>();
        var executor = Executors.newFixedThreadPool(2);
        Future<?> primeiraExecucao = null;
        Future<Boolean> segundaExecucao = null;
        try {
            primeiraExecucao = executor.submit(() -> new TransactionTemplate(transactionManager)
                    .executeWithoutResult(transaction -> {
                        limitarEsperaNoBanco();
                        executar(primeira);
                        // O service ja terminou, mas a transacao continua segurando as travas.
                        primeiraAplicada.countDown();
                        aguardar(liberarPrimeira);
                    }));
            assertThat(primeiraAplicada.await(10, TimeUnit.SECONDS))
                    .as("a primeira operacao deve concluir antes de iniciar a disputa").isTrue();

            segundaExecucao = executor.submit(() -> {
                try {
                    new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
                        limitarEsperaNoBanco();
                        pidSegunda.complete(jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class));
                        executar(segunda);
                    });
                    return true;
                } catch (RegraNegocioException e) {
                    assertThat(e.getMessage()).contains("não pode ser alterada");
                    return false;
                }
            });

            String consultaBloqueada = aguardarBloqueioReal(pidSegunda.get(10, TimeUnit.SECONDS));
            // Sem a trava da comanda, a segunda operacao pode parar somente no produto
            // ou terminar prematuramente. Nenhum dos casos protege o status da comanda.
            assertThat(consultaBloqueada).containsPattern("(?is)\\bfrom\\s+comanda\\b")
                    .containsPattern("(?is)\\bfor\\s+(no\\s+key\\s+)?update\\b");
            assertThat(segundaExecucao.isDone()).isFalse();

            liberarPrimeira.countDown();
            primeiraExecucao.get(10, TimeUnit.SECONDS);
            assertThat(segundaExecucao.get(10, TimeUnit.SECONDS)).isEqualTo(segundaPermitida);

            // Consultas fora das transacoes concorrentes verificam o estado efetivamente commitado.
            assertThat(jdbc.queryForObject("SELECT status FROM comanda WHERE id = ?", String.class, comandaId))
                    .isEqualTo(statusFinal.name());
            assertThat(jdbc.queryForObject("SELECT estoque FROM produto WHERE id = ?", Long.class, produtoId))
                    .isEqualTo(estoqueFinal);
            assertThat(jdbc.queryForObject("""
                    SELECT COALESCE(SUM(quantidade), 0) FROM item_comanda WHERE comanda_id = ?
                    """, Long.class, comandaId)).isEqualTo(quantidadeFinal);
            assertThat(jdbc.queryForObject("""
                    SELECT data_fechamento IS NOT NULL FROM comanda WHERE id = ?
                    """, Boolean.class, comandaId)).isTrue();
        } finally {
            liberarPrimeira.countDown();
            if (primeiraExecucao != null && !primeiraExecucao.isDone()) {
                primeiraExecucao.cancel(true);
            }
            if (segundaExecucao != null && !segundaExecucao.isDone()) {
                segundaExecucao.cancel(true);
            }
            executor.shutdownNow();
            assertThat(executor.awaitTermination(20, TimeUnit.SECONDS)).isTrue();
        }
    }

    private void executar(Operacao operacao) {
        switch (operacao) {
            case CANCELAR -> service.cancelarComanda(comandaId);
            case ADICIONAR -> service.adicionarItem(comandaId, new ItemRequest(produtoId, 1));
            case FECHAR -> service.fecharComanda(comandaId);
            case REMOVER -> service.removerItem(comandaId, itemId);
        }
    }

    private void limitarEsperaNoBanco() {
        jdbc.execute("SET LOCAL lock_timeout = '10s'");
        jdbc.execute("SET LOCAL statement_timeout = '15s'");
    }

    private String aguardarBloqueioReal(int pid) throws InterruptedException {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        do {
            var consultas = jdbc.queryForList("""
                    SELECT query FROM pg_stat_activity
                    WHERE pid = ? AND cardinality(pg_blocking_pids(pid)) > 0
                    """, String.class, pid);
            if (!consultas.isEmpty()) {
                return consultas.getFirst();
            }
            // Polling do estado real do PostgreSQL, sem presumir que dormir causou concorrencia.
            Thread.sleep(20);
        } while (System.nanoTime() < limite);
        throw new AssertionError("A segunda operacao nao aguardou uma trava no PostgreSQL.");
    }

    private static void aguardar(CountDownLatch latch) {
        try {
            if (!latch.await(15, TimeUnit.SECONDS)) {
                throw new AssertionError("Tempo esgotado aguardando liberacao da primeira transacao.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Transacao interrompida.", e);
        }
    }

    private long inserir(String sql, Object... argumentos) {
        return jdbc.queryForObject(sql, Long.class, argumentos);
    }

    enum Operacao {
        CANCELAR, ADICIONAR, FECHAR, REMOVER
    }
}
