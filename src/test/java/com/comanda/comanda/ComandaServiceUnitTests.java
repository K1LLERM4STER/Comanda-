package com.comanda.comanda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.comanda.comanda.Model.Comanda;
import com.comanda.comanda.Model.Estabelecimento;
import com.comanda.comanda.Model.Grupo;
import com.comanda.comanda.Model.ItemComanda;
import com.comanda.comanda.Model.Mesa;
import com.comanda.comanda.Model.Produto;
import com.comanda.comanda.Model.StatusComanda;
import com.comanda.comanda.Model.Usuario;
import com.comanda.comanda.dto.request.ItemRequest;
import com.comanda.comanda.exception.RegraNegocioException;
import com.comanda.comanda.repository.ClienteRepository;
import com.comanda.comanda.repository.ComandaRepository;
import com.comanda.comanda.repository.MesaRepository;
import com.comanda.comanda.repository.ProdutoRepository;
import com.comanda.comanda.repository.UsuarioRepository;
import com.comanda.comanda.service.ComandaService;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class ComandaServiceUnitTests {
    @Mock ComandaRepository repository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock MesaRepository mesaRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock ProdutoRepository produtoRepository;
    @Mock EntityManager entityManager;
    @InjectMocks ComandaService service;

    Comanda comanda;
    Produto produto;

    @BeforeEach
    void prepararComanda() {
        Estabelecimento estabelecimento = new Estabelecimento();
        ReflectionTestUtils.setField(estabelecimento, "id", 1L);
        Mesa mesa = new Mesa(estabelecimento, "1");
        Usuario usuario = new Usuario("Ana", "ana@example.com", "hash", estabelecimento);
        comanda = new Comanda(usuario, estabelecimento, mesa, null);
        produto = new Produto(new Grupo("Bebidas", estabelecimento), "Agua", 10L,
                new BigDecimal("5.00"), "UN", estabelecimento);
        ReflectionTestUtils.setField(produto, "id", 2L);
        when(repository.buscarComTrava(1L)).thenReturn(Optional.of(comanda));
    }

    @Test
    void naoDeveAlterarComandaFechadaNemConsultarEstoque() {
        comanda.fechar();

        assertThatThrownBy(() -> service.adicionarItem(1L, new ItemRequest(2L, 1)))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.removerItem(1L, 1L))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.cancelarComanda(1L))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.fecharComanda(1L))
                .isInstanceOf(RegraNegocioException.class);
        verifyNoInteractions(produtoRepository);
    }

    @Test
    void naoDeveVenderSemEstoqueSuficiente() {
        when(produtoRepository.buscarComTrava(2L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> service.adicionarItem(1L, new ItemRequest(2L, 11)))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("Estoque insuficiente");
        assertThat(produto.getEstoque()).isEqualTo(10L);
        assertThat(comanda.getItens()).isEmpty();
    }

    @Test
    void naoDeveEstourarQuantidadeAcumuladaDoItem() {
        ItemComanda item = new ItemComanda(produto, Integer.MAX_VALUE, produto.getPvenda(), comanda);
        comanda.adicionarItem(item);
        when(produtoRepository.buscarComTrava(2L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> service.adicionarItem(1L, new ItemRequest(2L, 1)))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("limite permitido por item");
        assertThat(item.getQuantidade()).isEqualTo(Integer.MAX_VALUE);
        assertThat(produto.getEstoque()).isEqualTo(10L);
    }

    @Test
    void naoDeveEstourarEstoqueAoCancelar() {
        produto.setEstoque(Long.MAX_VALUE);
        comanda.adicionarItem(new ItemComanda(produto, 1, produto.getPvenda(), comanda));
        when(produtoRepository.buscarComTrava(2L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> service.cancelarComanda(1L))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("limite de estoque");
        assertThat(produto.getEstoque()).isEqualTo(Long.MAX_VALUE);
        assertThat(comanda.getStatus()).isEqualTo(StatusComanda.ABERTA);
    }

    @Test
    void deveManterItensSeparadosQuandoPrecoMudar() {
        comanda.adicionarItem(new ItemComanda(produto, 1, new BigDecimal("4.00"), comanda));
        when(produtoRepository.buscarComTrava(2L)).thenReturn(Optional.of(produto));
        when(repository.saveAndFlush(comanda)).thenReturn(comanda);

        var resposta = service.adicionarItem(1L, new ItemRequest(2L, 2));

        assertThat(resposta.itens()).hasSize(2);
        assertThat(resposta.total()).isEqualByComparingTo("14.00");
        assertThat(produto.getEstoque()).isEqualTo(8L);
    }

    @Test
    void fecharPreservaEstoqueERegistraData() {
        when(repository.save(comanda)).thenReturn(comanda);
        var resposta = service.fecharComanda(1L);
        assertThat(resposta.status()).isEqualTo(StatusComanda.FECHADA);
        assertThat(resposta.dataFechamento()).isNotNull();
        verifyNoInteractions(produtoRepository);
    }

    @Test
    void cancelarDevolveEstoque() {
        comanda.adicionarItem(new ItemComanda(produto, 3, produto.getPvenda(), comanda));
        when(produtoRepository.buscarComTrava(2L)).thenReturn(Optional.of(produto));
        when(repository.save(comanda)).thenReturn(comanda);
        var resposta = service.cancelarComanda(1L);
        assertThat(resposta.status()).isEqualTo(StatusComanda.CANCELADA);
        assertThat(produto.getEstoque()).isEqualTo(13L);
    }

    @Test
    void removerItemDevolveEstoqueERetornaComandaAtualizada() {
        var item = new ItemComanda(produto, 2, produto.getPvenda(), comanda);
        ReflectionTestUtils.setField(item, "id", 7L);
        comanda.adicionarItem(item);
        when(produtoRepository.buscarComTrava(2L)).thenReturn(Optional.of(produto));
        when(repository.saveAndFlush(comanda)).thenReturn(comanda);
        var resposta = service.removerItem(1L, 7L);
        assertThat(resposta.itens()).isEmpty();
        assertThat(produto.getEstoque()).isEqualTo(12L);
    }
}
