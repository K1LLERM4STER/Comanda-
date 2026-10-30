package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(example = "{\"numeroMesa\": \"01\", \"estabelecimentoId\": 1}")
public record MesaCadastroRequest(
        @NotBlank @Size(max = 255) String numeroMesa,
        @NotNull @Positive @Schema(description = "ID de um registro que precisa existir.", example = "1") Long estabelecimentoId) {}
