package com.comanda.comanda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.comanda.comanda.Model.Estabelecimento;
import com.comanda.comanda.Model.Grupo;
import com.comanda.comanda.Model.Produto;
import com.comanda.comanda.dto.request.AjusteEstoqueRequest;
import com.comanda.comanda.dto.request.ProdutoAtualizacaoRequest;
import com.comanda.comanda.exception.RecursoNaoEncontradoException;
import com.comanda.comanda.exception.RegraNegocioException;
import com.comanda.comanda.repository.EstabelecimentoRepository;
import com.comanda.comanda.repository.GrupoRepository;
import com.comanda.comanda.repository.ProdutoRepository;
import com.comanda.comanda.service.ProdutoService;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceUnitTests {

    @Mock
    ProdutoRepository repository;
    @Mock
    GrupoRepository grupoRepository;
    @Mock
    EstabelecimentoRepository estabelecimentoRepository;
    @Mock
    EntityManager entityManager;
    @InjectMocks
    ProdutoService service;

    Produto produto;
    Grupo grupo;
    Estabelecimento estabelecimento;

    @BeforeEach
    void prepararProduto() {
        estabelecimento = new Estabelecimento();
        ReflectionTestUtils.setField(estabelecimento, "id", 1L);
        grupo = new Grupo("Bebidas", estabelecimento);
        ReflectionTestUtils.setField(grupo, "id", 2L);
        produto = new Produto(grupo, "Agua", 10L, new BigDecimal("5.00"), "UN", estabelecimento);
    }

    @Test
    void editarCadastroDevePreservarEstoqueAtual() {
        var dados = new ProdutoAtualizacaoRequest("Agua mineral", "GARRAFA", new BigDecimal("6.00"), 2L);
        when(repository.buscarComTrava(3L)).thenReturn(Optional.of(produto));
        when(grupoRepository.findById(2L)).thenReturn(Optional.of(grupo));
        when(repository.save(produto)).thenReturn(produto);

        var atualizado = service.atualizarProduto(3L, dados);

        assertThat(atualizado.getEstoque()).isEqualTo(10L);
        assertThat(atualizado.getDescricao()).isEqualTo("Agua mineral");
        assertThat(atualizado.getUnidade()).isEqualTo("GARRAFA");
        assertThat(atualizado.getPvenda()).isEqualByComparingTo("6.00");
        var ordem = inOrder(repository, entityManager);
        ordem.verify(repository).buscarComTrava(3L);
        ordem.verify(entityManager).refresh(produto);
        ordem.verify(repository).save(produto);
        verify(repository, never()).findById(3L);
    }

    @ParameterizedTest
    @CsvSource({"5,15", "-3,7", "-10,0", "9223372036854775797,9223372036854775807"})
    void ajusteDeveSomarVariacaoAoSaldoAtual(long variacao, long esperado) {
        when(repository.buscarComTrava(3L)).thenReturn(Optional.of(produto));
        when(repository.save(produto)).thenReturn(produto);

        var ajustado = service.ajustarEstoque(3L, new AjusteEstoqueRequest(variacao));

        assertThat(ajustado.getEstoque()).isEqualTo(esperado);
        assertThat(ajustado.getDescricao()).isEqualTo("Agua");
        assertThat(ajustado.getGrupo()).isSameAs(grupo);
        assertThat(ajustado.getEstabelecimento()).isSameAs(estabelecimento);
        assertThat(ajustado.getPvenda()).isEqualByComparingTo("5.00");
    }

    @Test
    void ajusteDeveUsarSaldoRecarregadoDepoisDaTrava() {
        when(repository.buscarComTrava(3L)).thenReturn(Optional.of(produto));
        when(repository.save(produto)).thenReturn(produto);
        // Simula uma venda anterior: o saldo mais recente no banco e 7, e nao 10.
        doAnswer(invocation -> {
            produto.setEstoque(7L);
            return null;
        }).when(entityManager).refresh(produto);

        service.ajustarEstoque(3L, new AjusteEstoqueRequest(2L));

        assertThat(produto.getEstoque()).isEqualTo(9L);
        var ordem = inOrder(repository, entityManager);
        ordem.verify(repository).buscarComTrava(3L);
        ordem.verify(entityManager).refresh(produto);
        ordem.verify(repository).save(produto);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = 0)
    void ajusteSemVariacaoDeveSerRejeitado(Long variacao) {
        assertThatThrownBy(() -> service.ajustarEstoque(3L, new AjusteEstoqueRequest(variacao)))
                .isInstanceOf(RegraNegocioException.class);
        verifyNoInteractions(repository, entityManager);
        assertThat(produto.getEstoque()).isEqualTo(10L);
    }

    @Test
    void retiradaMaiorQueSaldoNaoDeveAlterarProduto() {
        when(repository.buscarComTrava(3L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> service.ajustarEstoque(3L, new AjusteEstoqueRequest(-11L)))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("negativo");

        assertThat(produto.getEstoque()).isEqualTo(10L);
        verify(repository, never()).save(produto);
    }

    @Test
    void estouroNumericoNaoDeveAlterarProduto() {
        when(repository.buscarComTrava(3L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> service.ajustarEstoque(3L, new AjusteEstoqueRequest(Long.MAX_VALUE)))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("limite");

        assertThat(produto.getEstoque()).isEqualTo(10L);
        verify(repository, never()).save(produto);
    }

    @Test
    void ajusteDeProdutoInexistenteDeveSerRejeitado() {
        when(repository.buscarComTrava(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ajustarEstoque(3L, new AjusteEstoqueRequest(1L)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(entityManager);
    }

    @Test
    void edicaoDeveContinuarRejeitandoGrupoDeOutroEstabelecimento() {
        var outroEstabelecimento = new Estabelecimento();
        ReflectionTestUtils.setField(outroEstabelecimento, "id", 9L);
        var outroGrupo = new Grupo("Outro grupo", outroEstabelecimento);
        ReflectionTestUtils.setField(outroGrupo, "id", 8L);
        var dados = new ProdutoAtualizacaoRequest("Outra descricao", "UN", BigDecimal.ONE, 8L);
        when(repository.buscarComTrava(3L)).thenReturn(Optional.of(produto));
        when(grupoRepository.findById(8L)).thenReturn(Optional.of(outroGrupo));

        assertThatThrownBy(() -> service.atualizarProduto(3L, dados))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(produto.getDescricao()).isEqualTo("Agua");
        assertThat(produto.getEstoque()).isEqualTo(10L);
        verify(repository, never()).save(produto);
    }
}
