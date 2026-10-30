package com.comanda.comanda.dto.request;

import org.hibernate.validator.constraints.br.CPF;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteAtualizacaoRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @CPF @Size(max = 255) String cpf) {
}
