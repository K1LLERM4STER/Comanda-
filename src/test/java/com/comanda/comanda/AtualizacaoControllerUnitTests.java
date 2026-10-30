package com.comanda.comanda;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.comanda.comanda.controller.ClienteController;
import com.comanda.comanda.controller.EstabelecimentoController;
import com.comanda.comanda.controller.GrupoController;
import com.comanda.comanda.controller.MesaController;
import com.comanda.comanda.controller.ProdutoController;
import com.comanda.comanda.controller.UsuarioController;
import com.comanda.comanda.dto.request.ClienteAtualizacaoRequest;
import com.comanda.comanda.dto.request.EstabelecimentoAtualizacaoRequest;
import com.comanda.comanda.dto.request.GrupoAtualizacaoRequest;
import com.comanda.comanda.dto.request.MesaAtualizacaoRequest;
import com.comanda.comanda.dto.request.ProdutoAtualizacaoRequest;
import com.comanda.comanda.dto.request.UsuarioAtualizacaoRequest;
import com.comanda.comanda.exception.GlobalExceptionHandler;
import com.comanda.comanda.service.ClienteService;
import com.comanda.comanda.service.EstabelecimentoService;
import com.comanda.comanda.service.GrupoService;
import com.comanda.comanda.service.MesaService;
import com.comanda.comanda.service.ProdutoService;
import com.comanda.comanda.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class AtualizacaoControllerUnitTests {

    @Mock ClienteService clientes;
    @Mock UsuarioService usuarios;
    @Mock EstabelecimentoService estabelecimentos;
    @Mock GrupoService grupos;
    @Mock MesaService mesas;
    @Mock ProdutoService produtos;
    MockMvc mvc;

    @BeforeEach
    void configurarControllersSemBanco() {
        mvc = MockMvcBuilders.standaloneSetup(new ClienteController(clientes), new UsuarioController(usuarios),
                new EstabelecimentoController(estabelecimentos), new GrupoController(grupos),
                new MesaController(mesas), new ProdutoController(produtos))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    static Stream<Arguments> entradasInvalidas() {
        String textoLongo = "a".repeat(256);
        return Stream.of(
                Arguments.of("usuarios", "{}", "nome"),
                Arguments.of("usuarios", "{\"nome\":\"   \"}", "nome"),
                Arguments.of("usuarios", "{\"nome\":\"" + textoLongo + "\"}", "nome"),
                Arguments.of("clientes", "{}", "nome"),
                Arguments.of("clientes", "{\"nome\":\"Ana\",\"email\":\"invalido\",\"cpf\":\"52998224725\"}", "email"),
                Arguments.of("clientes", "{\"nome\":\"Ana\",\"email\":\"ana@example.com\",\"cpf\":\"123\"}", "cpf"),
                Arguments.of("estabelecimentos", "{}", "endereco"),
                Arguments.of("estabelecimentos", "{\"nome\":\"Bar\",\"endereco\":\"Rua\",\"email\":\"bar@example.com\",\"sobre\":\""
                        + textoLongo + "\"}", "sobre"),
                Arguments.of("grupos", "{}", "descricao"),
                Arguments.of("mesas", "{}", "numeroMesa"),
                Arguments.of("produtos", "{}", "pvenda"),
                Arguments.of("produtos", produtoJson("0", "2"), "pvenda"),
                Arguments.of("produtos", produtoJson("-1", "2"), "pvenda"),
                Arguments.of("produtos", produtoJson("1.001", "2"), "pvenda"),
                Arguments.of("produtos", produtoJson("100000000", "2"), "pvenda"),
                Arguments.of("produtos", produtoJson("5.00", "0"), "grupoId"),
                Arguments.of("produtos", produtoJson("5.00", "null"), "grupoId"));
    }

    @ParameterizedTest(name = "{0}: rejeita campo {2} invalido")
    @MethodSource("entradasInvalidas")
    void deveRetornar400AntesDeChamarService(String recurso, String corpo, String campo) throws Exception {
        mvc.perform(put("/" + recurso + "/1").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros." + campo).exists());
        verifyNoInteractions(clientes, usuarios, estabelecimentos, grupos, mesas, produtos);
    }

    @Test
    void usuarioDeveExigirSomenteNomeSemSenhaOuEstabelecimento() throws Exception {
        atualizar("usuarios", "{\"nome\":\"Ana\"}");
        verify(usuarios).atualizarUsuario(1L, new UsuarioAtualizacaoRequest("Ana"));
    }

    @Test
    void nomeNoLimiteDoBancoDeveSerAceito() throws Exception {
        String nome = "a".repeat(255);
        atualizar("usuarios", "{\"nome\":\"" + nome + "\"}");
        verify(usuarios).atualizarUsuario(1L, new UsuarioAtualizacaoRequest(nome));
    }

    @Test
    void clienteDeveSerAtualizadoSemExigirSenha() throws Exception {
        atualizar("clientes", "{\"nome\":\"Ana\",\"email\":\"ana@example.com\",\"cpf\":\"52998224725\"}");
        verify(clientes).atualizarCliente(1L, new ClienteAtualizacaoRequest("Ana", "ana@example.com", "52998224725"));
    }

    @Test
    void estabelecimentoNaoDeveExigirCnpjOuSituacaoESobrePermaneceOpcional() throws Exception {
        atualizar("estabelecimentos", "{\"nome\":\"Bar\",\"endereco\":\"Rua\",\"email\":\"bar@example.com\"}");
        verify(estabelecimentos).atualizarEstabelecimento(1L,
                new EstabelecimentoAtualizacaoRequest("Bar", "Rua", null, "bar@example.com"));
    }

    @Test
    void grupoNaoDeveExigirEstabelecimento() throws Exception {
        atualizar("grupos", "{\"descricao\":\"Bebidas\"}");
        verify(grupos).atualizarGrupo(1L, new GrupoAtualizacaoRequest("Bebidas"));
    }

    @Test
    void mesaNaoDeveExigirEstabelecimento() throws Exception {
        atualizar("mesas", "{\"numeroMesa\":\"2\"}");
        verify(mesas).atualizarMesa(1L, new MesaAtualizacaoRequest("2"));
    }

    @Test
    void produtoNaoDeveExigirEstoqueOuEstabelecimentoEDeveAceitarPrecoLimite() throws Exception {
        atualizar("produtos", produtoJson("99999999.99", "2"));
        verify(produtos).atualizarProduto(1L,
                new ProdutoAtualizacaoRequest("Agua", "UN", new BigDecimal("99999999.99"), 2L));
    }

    private void atualizar(String recurso, String corpo) throws Exception {
        var estabelecimento = new com.comanda.comanda.Model.Estabelecimento();
        var grupo = new com.comanda.comanda.Model.Grupo("Bebidas", estabelecimento);
        switch (recurso) {
            case "usuarios" -> org.mockito.Mockito.when(usuarios.atualizarUsuario(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.comanda.comanda.Model.Usuario("Ana", "ana@example.com", "hash", estabelecimento));
            case "clientes" -> org.mockito.Mockito.when(clientes.atualizarCliente(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.comanda.comanda.Model.Cliente("Ana", "ana@example.com", "hash", "52998224725"));
            case "estabelecimentos" -> org.mockito.Mockito.when(estabelecimentos.atualizarEstabelecimento(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any())).thenReturn(estabelecimento);
            case "grupos" -> org.mockito.Mockito.when(grupos.atualizarGrupo(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any())).thenReturn(grupo);
            case "mesas" -> org.mockito.Mockito.when(mesas.atualizarMesa(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any())).thenReturn(new com.comanda.comanda.Model.Mesa(estabelecimento, "2"));
            case "produtos" -> org.mockito.Mockito.when(produtos.atualizarProduto(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.comanda.comanda.Model.Produto(grupo, "Agua", 10L, new BigDecimal("5.00"), "UN", estabelecimento));
        }
        mvc.perform(put("/" + recurso + "/1").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk());
    }

    private static String produtoJson(String preco, String grupoId) {
        return "{\"descricao\":\"Agua\",\"unidade\":\"UN\",\"pvenda\":" + preco + ",\"grupoId\":" + grupoId + "}";
    }
}
