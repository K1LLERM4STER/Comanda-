package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MesaAtualizacaoRequest(
        @NotBlank @Size(max = 255) String numeroMesa) {
}
