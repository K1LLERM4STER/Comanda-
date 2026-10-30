package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(example = "{\"nome\": \"João Exemplo\", \"email\": \"joao@example.com\", \"senha\": \"SenhaExemplo123\", \"estabelecimentoId\": 1}")
public record UsuarioCadastroRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 72) String senha,
        @NotNull @Positive @Schema(description = "ID de um registro que precisa existir.", example = "1") Long estabelecimentoId) {}
