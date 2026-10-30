package com.comanda.comanda.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.comanda.comanda.dto.request.ProdutoCadastroRequest;
import com.comanda.comanda.dto.response.ProdutoResponse;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comanda.comanda.dto.request.ProdutoAtualizacaoRequest;
import com.comanda.comanda.dto.request.AjusteEstoqueRequest;
import com.comanda.comanda.service.ProdutoService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Tag(name = "Produtos")
@RestController
@RequestMapping("/produtos")
@AllArgsConstructor
public class ProdutoController {

    private final ProdutoService service;

    @PostMapping
    @Operation(summary = "Cadastrar — Produtos", operationId = "produtoCadastrar")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Regra de negócio", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<ProdutoResponse> cadastrar(@Valid @RequestBody ProdutoCadastroRequest produto) {
        var resposta = ProdutoResponse.de(service.salvarProduto(produto));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resposta.id()).toUri()).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar — Produtos", operationId = "produtoListar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Grupo não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<List<ProdutoResponse>> listar(@RequestParam(required = false) @Positive Long grupoId) {
        return ResponseEntity.ok((grupoId == null ? service.listarProdutos() : service.buscarPorGrupoId(grupoId)).stream().map(ProdutoResponse::de).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID — Produtos", operationId = "produtoBuscarPorId")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ProdutoResponse.de(service.buscarPorId(id)));
    }


    @PutMapping("/{id}")
    @Operation(summary = "Atualizar — Produtos", operationId = "produtoAtualizar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Regra de negócio", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable @Positive Long id, @Valid @RequestBody ProdutoAtualizacaoRequest novosDados) {
        return ResponseEntity.ok(ProdutoResponse.de(service.atualizarProduto(id, novosDados)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir — Produtos", operationId = "produtoDeletar")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sucesso", content = @Content),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable @Positive Long id) {
        service.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estoque")
    @Operation(summary = "Ajustar estoque — Produtos", operationId = "produtoAjustarEstoque")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Regra de negócio", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<ProdutoResponse> ajustarEstoque(@PathVariable @Positive Long id,
            @Valid @RequestBody AjusteEstoqueRequest request) {
        return ResponseEntity.ok(ProdutoResponse.de(service.ajustarEstoque(id, request)));
    }
}
