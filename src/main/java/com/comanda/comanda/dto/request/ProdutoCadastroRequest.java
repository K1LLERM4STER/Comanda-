package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(example = "{\"descricao\": \"Água mineral\", \"unidade\": \"UN\", \"estoque\": 20, \"pvenda\": 5.5, \"grupoId\": 1, \"estabelecimentoId\": 1}")
public record ProdutoCadastroRequest(
        @NotBlank @Size(max = 255) String descricao,
        @NotBlank @Size(max = 255) String unidade,
        @NotNull @PositiveOrZero Long estoque,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal pvenda,
        @NotNull @Positive @Schema(description = "ID de um registro que precisa existir.", example = "1") Long grupoId,
        @NotNull @Positive @Schema(description = "ID de um registro que precisa existir.", example = "1") Long estabelecimentoId) {}
