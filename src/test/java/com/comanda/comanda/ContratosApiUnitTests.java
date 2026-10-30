package com.comanda.comanda;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.comanda.comanda.Model.*;
import com.comanda.comanda.controller.*;
import com.comanda.comanda.dto.request.*;
import com.comanda.comanda.exception.*;
import com.comanda.comanda.repository.*;
import com.comanda.comanda.service.*;

@ExtendWith(MockitoExtension.class)
class ContratosApiUnitTests {
    @Mock ProdutoRepository produtos;
    @Mock GrupoRepository grupos;
    @Mock EstabelecimentoRepository estabelecimentos;
    @Mock jakarta.persistence.EntityManager entityManager;
    @InjectMocks ProdutoService service;
    Estabelecimento estabelecimento;
    Grupo grupo;
    ProdutoCadastroRequest request = new ProdutoCadastroRequest("Agua", "UN", 20L, new BigDecimal("5.50"), 2L, 1L);

    @BeforeEach void preparar() {
        estabelecimento = new Estabelecimento("Bar", "11222333000181", null, "Rua", "bar@example.com");
        ReflectionTestUtils.setField(estabelecimento, "id", 1L);
        grupo = new Grupo("Bebidas", estabelecimento);
        ReflectionTestUtils.setField(grupo, "id", 2L);
    }
    @Test void cadastroResolveIdsEPreservaValores() {
        when(grupos.findById(2L)).thenReturn(Optional.of(grupo));
        when(estabelecimentos.findById(1L)).thenReturn(Optional.of(estabelecimento));
        when(produtos.save(any())).thenAnswer(i -> i.getArgument(0));
        Produto produto = service.salvarProduto(request);
        assertThat(produto.getGrupo()).isSameAs(grupo);
        assertThat(produto.getEstabelecimento()).isSameAs(estabelecimento);
        assertThat(produto.getId()).isNull();
        assertThat(produto.getPvenda()).isEqualByComparingTo("5.50");
        assertThat(produto.getEstoque()).isEqualTo(20L);
    }
    @Test void cadastroRejeitaGrupoInexistente() {
        when(grupos.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.salvarProduto(request)).isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(produtos);
    }
    @Test void cadastroRejeitaGrupoDeOutroEstabelecimento() {
        var outro = new Estabelecimento();
        ReflectionTestUtils.setField(outro, "id", 3L);
        when(grupos.findById(2L)).thenReturn(Optional.of(new Grupo("Outro", outro)));
        when(estabelecimentos.findById(1L)).thenReturn(Optional.of(estabelecimento));
        assertThatThrownBy(() -> service.salvarProduto(request)).isInstanceOf(RegraNegocioException.class);
        verifyNoInteractions(produtos);
    }
    @Test void cadastroHttpRetorna201LocationERespostaPlana() throws Exception {
        var mocked = mock(ProdutoService.class);
        Produto produto = new Produto(grupo, "Agua", 20L, new BigDecimal("5.50"), "UN", estabelecimento);
        ReflectionTestUtils.setField(produto, "id", 9L);
        when(mocked.salvarProduto(request)).thenReturn(produto);
        mvc(new ProdutoController(mocked)).perform(post("/produtos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"descricao\":\"Agua\",\"unidade\":\"UN\",\"estoque\":20,\"pvenda\":5.50,\"grupoId\":2,\"estabelecimentoId\":1}"))
            .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/produtos/9"))
            .andExpect(jsonPath("$.grupoId").value(2)).andExpect(jsonPath("$.grupo").doesNotExist())
            .andExpect(jsonPath("$.estabelecimento").doesNotExist());
    }
    @Test void usuarioNuncaRetornaSenhaOuHash() throws Exception {
        var mocked = mock(UsuarioService.class);
        when(mocked.buscarPorId(1L)).thenReturn(new Usuario("Ana", "ana@example.com", "$2a$hash", estabelecimento));
        mvc(new UsuarioController(mocked)).perform(get("/usuarios/1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.senha").doesNotExist())
            .andExpect(jsonPath("$.hash").doesNotExist()).andExpect(jsonPath("$.estabelecimentoId").value(1));
    }
    @Test void filtroProdutosUsaGrupoENaoListaGeral() throws Exception {
        var mocked = mock(ProdutoService.class);
        when(mocked.buscarPorGrupoId(2L)).thenReturn(List.of());
        mvc(new ProdutoController(mocked)).perform(get("/produtos").param("grupoId", "2")).andExpect(status().isOk());
        verify(mocked).buscarPorGrupoId(2L);
        verify(mocked, never()).listarProdutos();
    }
    @Test void filtroMesasPreservaListaVazia() throws Exception {
        var mocked = mock(MesaService.class);
        when(mocked.listarPorEstabelecimento(99L)).thenReturn(List.of());
        mvc(new MesaController(mocked)).perform(get("/mesas").param("estabelecimentoId", "99"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    @Test void parametrosInvalidosUsamProblemDetail() throws Exception {
        var mocked = mock(ComandaService.class);
        MockMvc mvc = mvc(new ComandaController(mocked));
        for (String url : List.of("/comandas/0", "/comandas/abc", "/comandas?status=INVALIDO", "/comandas?estabelecimentoId=-1")) {
            mvc.perform(get(url)).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        }
        verifyNoInteractions(mocked);
    }
    @Test void exclusaoEAtivacaoRetornam204SemCorpo() throws Exception {
        var clientes = mock(ClienteService.class);
        mvc(new ClienteController(clientes)).perform(delete("/clientes/1"))
            .andExpect(status().isNoContent()).andExpect(content().string(""));
        var estabs = mock(EstabelecimentoService.class);
        mvc(new EstabelecimentoController(estabs)).perform(patch("/estabelecimentos/1/desativar"))
            .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(clientes).deletarCliente(1L);
        verify(estabs).desativarEstabelecimento(1L);
    }

    @Test void aberturaSemClienteEMesaOcupada() {
        var repositorio = mock(ComandaRepository.class);
        var usuarios = mock(UsuarioRepository.class);
        var mesas = mock(MesaRepository.class);
        var clientes = mock(ClienteRepository.class);
        var comandaService = new ComandaService(repositorio, usuarios, mesas, clientes, produtos, entityManager);
        var mesa = new Mesa(estabelecimento, "1");
        ReflectionTestUtils.setField(mesa, "id", 4L);
        var usuario = new Usuario("Ana", "ana@example.com", "hash", estabelecimento);
        ReflectionTestUtils.setField(usuario, "id", 5L);
        when(mesas.findById(4L)).thenReturn(Optional.of(mesa));
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario));
        when(repositorio.save(any())).thenAnswer(i -> i.getArgument(0));
        var resposta = comandaService.abrirComanda(new ComandaRequest(5L, 4L, null));
        assertThat(resposta.clienteId()).isNull();
        assertThat(resposta.status()).isEqualTo(StatusComanda.ABERTA);
        verifyNoInteractions(clientes);
        when(repositorio.existsByMesaIdAndStatus(4L, StatusComanda.ABERTA)).thenReturn(true);
        assertThatThrownBy(() -> comandaService.abrirComanda(new ComandaRequest(5L, 4L, null)))
            .isInstanceOf(ConflitoException.class);
    }
    private MockMvc mvc(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler()).build();
    }
}
