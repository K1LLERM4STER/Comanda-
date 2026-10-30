package com.comanda.comanda;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.comanda.comanda.Model.Produto;
import com.comanda.comanda.controller.ProdutoController;
import com.comanda.comanda.dto.request.AjusteEstoqueRequest;
import com.comanda.comanda.exception.GlobalExceptionHandler;
import com.comanda.comanda.exception.RegraNegocioException;
import com.comanda.comanda.service.ProdutoService;

@ExtendWith(MockitoExtension.class)
class ProdutoEstoqueControllerUnitTests {

    @Mock
    ProdutoService service;
    MockMvc mvc;

    @BeforeEach
    void configurarControllerSemContextoSpring() {
        mvc = MockMvcBuilders.standaloneSetup(new ProdutoController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void variacaoAusenteDeveRetornar400SemChamarService() throws Exception {
        mvc.perform(patch("/produtos/3/estoque").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros.variacao").exists());
        verifyNoInteractions(service);
    }

    @Test
    void ajusteValidoDeveRetornar200ComNovoSaldo() throws Exception {
        var estabelecimento = new com.comanda.comanda.Model.Estabelecimento();
        var grupo = new com.comanda.comanda.Model.Grupo("Bebidas", estabelecimento);
        var produto = new Produto(grupo, "Agua", 10L, new java.math.BigDecimal("5.00"), "UN", estabelecimento);
        produto.setEstoque(8L);
        when(service.ajustarEstoque(3L, new AjusteEstoqueRequest(-2L))).thenReturn(produto);

        mvc.perform(patch("/produtos/3/estoque").contentType(MediaType.APPLICATION_JSON)
                .content("{\"variacao\":-2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estoque").value(8));
        verify(service).ajustarEstoque(3L, new AjusteEstoqueRequest(-2L));
    }

    @Test
    void ajusteInvalidoDeveUsarTratamento422Existente() throws Exception {
        when(service.ajustarEstoque(3L, new AjusteEstoqueRequest(0L)))
                .thenThrow(new RegraNegocioException("Informe uma variação de estoque diferente de zero."));

        mvc.perform(patch("/produtos/3/estoque").contentType(MediaType.APPLICATION_JSON)
                .content("{\"variacao\":0}"))
                .andExpect(status().is(422)).andExpect(jsonPath("$.status").value(422));
    }
}
