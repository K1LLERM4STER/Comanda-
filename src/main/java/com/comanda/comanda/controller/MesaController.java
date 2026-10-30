package com.comanda.comanda.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.comanda.comanda.dto.request.MesaCadastroRequest;
import com.comanda.comanda.dto.response.MesaResponse;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comanda.comanda.dto.request.MesaAtualizacaoRequest;
import com.comanda.comanda.service.MesaService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Tag(name = "Mesas")
@RestController
@RequestMapping("/mesas")
@AllArgsConstructor
public class MesaController {

    private final MesaService service;

    @PostMapping
    @Operation(summary = "Cadastrar — Mesas", operationId = "mesaCadastrar")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<MesaResponse> cadastrar(@Valid @RequestBody MesaCadastroRequest mesa) {
        var resposta = MesaResponse.de(service.salvarMesa(mesa));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resposta.id()).toUri()).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar — Mesas", operationId = "mesaListar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<List<MesaResponse>> listar(@RequestParam(required = false) @Positive Long estabelecimentoId) {
        return ResponseEntity.ok((estabelecimentoId == null ? service.listarMesas() : service.listarPorEstabelecimento(estabelecimentoId)).stream().map(MesaResponse::de).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID — Mesas", operationId = "mesaBuscarPorId")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<MesaResponse> buscarPorId(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(MesaResponse.de(service.buscarPorId(id)));
    }


    @PutMapping("/{id}")
    @Operation(summary = "Atualizar — Mesas", operationId = "mesaAtualizar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<MesaResponse> atualizar(@PathVariable @Positive Long id, @Valid @RequestBody MesaAtualizacaoRequest novosDados) {
        return ResponseEntity.ok(MesaResponse.de(service.atualizarMesa(id, novosDados)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir — Mesas", operationId = "mesaDeletar")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sucesso", content = @Content),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable @Positive Long id) {
        service.deletarMesa(id);
        return ResponseEntity.noContent().build();
    }
}
