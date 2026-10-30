package com.comanda.comanda.dto.request;

import jakarta.validation.constraints.NotNull;

// Positivo acrescenta unidades; negativo retira unidades do saldo atual.
public record AjusteEstoqueRequest(@NotNull Long variacao) {
}
