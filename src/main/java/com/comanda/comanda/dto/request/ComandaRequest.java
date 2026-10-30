package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ComandaRequest(
        @NotNull @Positive @io.swagger.v3.oas.annotations.media.Schema(description = "ID de registro existente; clienteId é opcional na abertura.", example = "1") Long usuarioId,
        @NotNull @Positive @io.swagger.v3.oas.annotations.media.Schema(description = "ID de registro existente; clienteId é opcional na abertura.", example = "1") Long mesaId,
        @Positive @io.swagger.v3.oas.annotations.media.Schema(description = "ID de registro existente; clienteId é opcional na abertura.", example = "1") Long clienteId) {
}
