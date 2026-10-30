package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(example = "{\"nome\": \"Ana Exemplo\", \"email\": \"ana@example.com\", \"senha\": \"SenhaExemplo123\", \"cpf\": \"52998224725\"}")
public record ClienteCadastroRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 72) String senha,
        @NotBlank @CPF @Size(max = 255) String cpf) {}
