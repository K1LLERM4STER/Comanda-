package com.comanda.comanda.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.comanda.comanda.dto.request.UsuarioCadastroRequest;
import com.comanda.comanda.dto.response.UsuarioResponse;
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

import com.comanda.comanda.dto.request.UsuarioAtualizacaoRequest;
import com.comanda.comanda.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Tag(name = "Usuários")
@RestController
@RequestMapping("/usuarios")
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    @Operation(summary = "Cadastrar — Usuários", operationId = "usuarioCadastrar")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Regra de negócio", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioCadastroRequest usuario) {
        var resposta = UsuarioResponse.de(service.salvarUsuario(usuario));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resposta.id()).toUri()).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar — Usuários", operationId = "usuarioListar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true)
    })
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(service.listarUsuarios().stream().map(UsuarioResponse::de).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID — Usuários", operationId = "usuarioBuscarPorId")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(UsuarioResponse.de(service.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar — Usuários", operationId = "usuarioAtualizar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sucesso", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable @Positive Long id, @Valid @RequestBody UsuarioAtualizacaoRequest novosDados) {
        return ResponseEntity.ok(UsuarioResponse.de(service.atualizarUsuario(id, novosDados)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir — Usuários", operationId = "usuarioDeletar")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sucesso", content = @Content),
        @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Registro duplicado ou em uso", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable @Positive Long id) {
        service.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
