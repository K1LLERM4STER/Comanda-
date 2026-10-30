package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(example = "{\"descricao\": \"Bebidas\", \"estabelecimentoId\": 1}")
public record GrupoCadastroRequest(
        @NotBlank @Size(max = 255) String descricao,
        @NotNull @Positive @Schema(description = "ID de um registro que precisa existir.", example = "1") Long estabelecimentoId) {}
