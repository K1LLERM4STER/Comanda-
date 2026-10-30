package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstabelecimentoAtualizacaoRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Size(max = 255) String endereco,
        @Size(max = 255) String sobre,
        @NotBlank @Email @Size(max = 255) String email) {
}
