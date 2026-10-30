package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GrupoAtualizacaoRequest(
        @NotBlank @Size(max = 255) String descricao) {
}
