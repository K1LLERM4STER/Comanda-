package com.comanda.comanda.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.comanda.comanda.dto.request.EstabelecimentoCadastroRequest;
import com.comanda.comanda.dto.response.EstabelecimentoResponse;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comanda.comanda.dto.request.EstabelecimentoAtualizacaoRequest;
import com.comanda.comanda.service.EstabelecimentoService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Tag(name = "Estabelecimentos")
@RestController
@RequestMapping("/estabelecimentos")
@AllArgsConstructor
public class EstabelecimentoController {

    private final EstabelecimentoService service;

    @PostMapping
    @Operation(summary = "Cadastrar — Estabelecimentos", operationId = "estabelecimentoCadastrar")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<EstabelecimentoResponse> cadastrar(@Valid @RequestBody EstabelecimentoCadastroRequest estabelecimento) {
        var resposta = EstabelecimentoResponse.de(service.salvarEstabelecimento(estabelecimento));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resposta.id()).toUri()).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar — Estabelecimentos", operationId = "estabelecimentoListar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true)
    })
    public ResponseEntity<List<EstabelecimentoResponse>> listar() {
        return ResponseEntity.ok(service.listarEstabelecimentos().stream().map(EstabelecimentoResponse::de).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID — Estabelecimentos", operationId = "estabelecimentoBuscarPorId")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<EstabelecimentoResponse> buscarPorId(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(EstabelecimentoResponse.de(service.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar — Estabelecimentos", operationId = "estabelecimentoAtualizar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<EstabelecimentoResponse> atualizar(@PathVariable @Positive Long id, @Valid @RequestBody EstabelecimentoAtualizacaoRequest novosDados) {
        return ResponseEntity.ok(EstabelecimentoResponse.de(service.atualizarEstabelecimento(id, novosDados)));
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar — Estabelecimentos", operationId = "estabelecimentoDesativar")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sucesso", content = @Content),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<Void> desativar(@PathVariable @Positive Long id) {
        service.desativarEstabelecimento(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    @Operation(summary = "Reativar — Estabelecimentos", operationId = "estabelecimentoReativar")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sucesso", content = @Content),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<Void> reativar(@PathVariable @Positive Long id) {
        service.reativarEstabelecimento(id);
        return ResponseEntity.noContent().build();
    }
}
