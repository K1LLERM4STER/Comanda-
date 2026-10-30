package com.comanda.comanda.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comanda.comanda.Model.StatusComanda;
import com.comanda.comanda.dto.request.ComandaRequest;
import com.comanda.comanda.dto.response.ComandaResponse;
import com.comanda.comanda.dto.request.ItemRequest;
import com.comanda.comanda.service.ComandaService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Tag(name = "Comandas")
@RestController
@RequestMapping("/comandas")
@AllArgsConstructor
public class ComandaController {

    private final ComandaService service;

    @PostMapping
    @Operation(summary = "Abrir — Comandas", operationId = "comandaAbrir")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Regra de negócio", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<ComandaResponse> abrir(@Valid @RequestBody ComandaRequest req) {
        var resposta = service.abrirComanda(req);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(resposta.id()).toUri()).body(resposta);
    }

    // Filtros opcionais: /comandas?estabelecimentoId=1&status=ABERTA
    @GetMapping
    @Operation(summary = "Listar — Comandas", operationId = "comandaListar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<List<ComandaResponse>> listar(
            @RequestParam(required = false) @Positive Long estabelecimentoId,
            @RequestParam(required = false) StatusComanda status) {
        return ResponseEntity.ok(service.listar(estabelecimentoId, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID — Comandas", operationId = "comandaBuscarPorId")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<ComandaResponse> buscarPorId(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping("/{id}/itens")
    @Operation(summary = "Adicionar item à comanda — Comandas", operationId = "comandaAdicionarItem")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Regra de negócio", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<ComandaResponse> adicionarItem(@PathVariable @Positive Long id, @Valid @RequestBody ItemRequest req) {
        return ResponseEntity.status(201).body(service.adicionarItem(id, req));
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    @Operation(summary = "Remover item da comanda — Comandas", operationId = "comandaRemoverItem")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Regra de negócio", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<ComandaResponse> removerItem(@PathVariable @Positive Long id, @PathVariable @Positive Long itemId) {
        return ResponseEntity.ok(service.removerItem(id, itemId));
    }

    @PatchMapping("/{id}/fechar")
    @Operation(summary = "Fechar comanda — Comandas", operationId = "comandaFechar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Regra de negócio", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<ComandaResponse> fechar(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.fecharComanda(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar comanda — Comandas", operationId = "comandaCancelar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Regra de negócio", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<ComandaResponse> cancelar(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.cancelarComanda(id));
    }
}
