package com.comanda.comanda.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.comanda.comanda.dto.request.GrupoCadastroRequest;
import com.comanda.comanda.dto.response.GrupoResponse;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comanda.comanda.dto.request.GrupoAtualizacaoRequest;
import com.comanda.comanda.service.GrupoService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Tag(name = "Grupos")
@RestController
@RequestMapping("/grupos")
@AllArgsConstructor
public class GrupoController {

    private final GrupoService service;

    @PostMapping
    @Operation(summary = "Cadastrar — Grupos", operationId = "grupoCadastrar")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<GrupoResponse> cadastrar(@Valid @RequestBody GrupoCadastroRequest grupo) {
        var resposta = GrupoResponse.de(service.salvarGrupo(grupo));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resposta.id()).toUri()).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar — Grupos", operationId = "grupoListar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true)
    })
    public ResponseEntity<List<GrupoResponse>> listar() {
        return ResponseEntity.ok(service.listarGrupos().stream().map(GrupoResponse::de).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID — Grupos", operationId = "grupoBuscarPorId")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<GrupoResponse> buscarPorId(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(GrupoResponse.de(service.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar — Grupos", operationId = "grupoAtualizar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<GrupoResponse> atualizar(@PathVariable @Positive Long id, @Valid @RequestBody GrupoAtualizacaoRequest novosDados) {
        return ResponseEntity.ok(GrupoResponse.de(service.atualizarGrupo(id, novosDados)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir — Grupos", operationId = "grupoDeletar")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sucesso", content = @Content),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable @Positive Long id) {
        service.deletarGrupo(id);
        return ResponseEntity.noContent().build();
    }
}
