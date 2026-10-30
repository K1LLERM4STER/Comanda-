package com.comanda.comanda;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.comanda.comanda.controller.ClienteController;
import com.comanda.comanda.controller.ComandaController;
import com.comanda.comanda.controller.EstabelecimentoController;
import com.comanda.comanda.controller.GrupoController;
import com.comanda.comanda.controller.MesaController;
import com.comanda.comanda.controller.ProdutoController;
import com.comanda.comanda.controller.UsuarioController;
import com.comanda.comanda.exception.GlobalExceptionHandler;
import com.comanda.comanda.service.ClienteService;
import com.comanda.comanda.service.ComandaService;
import com.comanda.comanda.service.EstabelecimentoService;
import com.comanda.comanda.service.GrupoService;
import com.comanda.comanda.service.MesaService;
import com.comanda.comanda.service.ProdutoService;
import com.comanda.comanda.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class CadastroControllerUnitTests {
    @Mock ClienteService clientes;
    @Mock UsuarioService usuarios;
    @Mock EstabelecimentoService estabelecimentos;
    @Mock GrupoService grupos;
    @Mock MesaService mesas;
    @Mock ProdutoService produtos;
    @Mock ComandaService comandas;
    MockMvc mvc;

    @BeforeEach
    void configurarControllersSemBanco() {
        mvc = MockMvcBuilders.standaloneSetup(new ClienteController(clientes), new UsuarioController(usuarios),
                new EstabelecimentoController(estabelecimentos), new GrupoController(grupos),
                new MesaController(mesas), new ProdutoController(produtos), new ComandaController(comandas))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    static Stream<Arguments> entradasInvalidas() {
        String longo = "a".repeat(256);
        return Stream.of(
                Arguments.of("/clientes", "{}", "cpf"),
                Arguments.of("/clientes", "{\"nome\":\"Ana\",\"email\":\"ana@example.com\",\"senha\":\"123\",\"cpf\":\"123\"}", "cpf"),
                Arguments.of("/clientes", "{\"nome\":\"" + longo + "\"}", "nome"),
                Arguments.of("/usuarios", "{\"email\":\"invalido\"}", "email"),
                Arguments.of("/usuarios", "{\"senha\":\"" + "a".repeat(73) + "\"}", "senha"),
                Arguments.of("/estabelecimentos", "{\"cnpj\":\"123\"}", "cnpj"),
                Arguments.of("/estabelecimentos", "{\"sobre\":\"" + longo + "\"}", "sobre"),
                Arguments.of("/grupos", "{\"descricao\":\"" + longo + "\"}", "descricao"),
                Arguments.of("/mesas", "{\"numeroMesa\":\"" + longo + "\"}", "numeroMesa"),
                Arguments.of("/produtos", "{\"pvenda\":1.001}", "pvenda"),
                Arguments.of("/produtos", "{\"pvenda\":100000000}", "pvenda"),
                Arguments.of("/produtos", "{\"estoque\":-1}", "estoque"),
                Arguments.of("/comandas", "{\"usuarioId\":0,\"mesaId\":1}", "usuarioId"),
                Arguments.of("/comandas", "{\"usuarioId\":1,\"mesaId\":-1}", "mesaId"),
                Arguments.of("/comandas", "{\"usuarioId\":1,\"mesaId\":1,\"clienteId\":0}", "clienteId"),
                Arguments.of("/comandas/1/itens", "{\"produtoId\":0,\"quantidade\":1}", "produtoId"),
                Arguments.of("/comandas/1/itens", "{\"produtoId\":1,\"quantidade\":0}", "quantidade"));
    }

    @ParameterizedTest(name = "{0}: rejeita campo {2} invalido")
    @MethodSource("entradasInvalidas")
    void deveRejeitarEntradaAntesDeChamarService(String url, String json, String campo) throws Exception {
        mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros." + campo).exists());
        verifyNoInteractions(clientes, usuarios, estabelecimentos, grupos, mesas, produtos, comandas);
    }
}
