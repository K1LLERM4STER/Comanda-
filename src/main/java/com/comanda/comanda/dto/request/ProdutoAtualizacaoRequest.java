package com.comanda.comanda.dto.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProdutoAtualizacaoRequest(
        @NotBlank @Size(max = 255) String descricao,
        @NotBlank @Size(max = 255) String unidade,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal pvenda,
        @NotNull @Positive @io.swagger.v3.oas.annotations.media.Schema(description = "ID de registro existente; clienteId é opcional na abertura.", example = "1") Long grupoId) {
}
