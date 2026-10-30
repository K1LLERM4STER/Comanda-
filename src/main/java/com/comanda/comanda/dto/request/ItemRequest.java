package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemRequest(
        @NotNull @Positive @io.swagger.v3.oas.annotations.media.Schema(description = "ID de registro existente; clienteId é opcional na abertura.", example = "1") Long produtoId,
        @NotNull @Positive Integer quantidade) {
}
