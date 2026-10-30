package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(example = "{\"nome\": \"Restaurante Exemplo\", \"cnpj\": \"11222333000181\", \"sobre\": \"Cozinha brasileira\", \"endereco\": \"Rua Exemplo, 100\", \"email\": \"contato@example.com\"}")
public record EstabelecimentoCadastroRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @CNPJ @Size(max = 255) String cnpj,
        @Size(max = 255) String sobre,
        @NotBlank @Size(max = 255) String endereco,
        @NotBlank @Email @Size(max = 255) String email) {}
