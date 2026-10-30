package com.comanda.comanda.dto.response;

import java.math.BigDecimal;

import com.comanda.comanda.Model.ItemComanda;

public record ItemResponse(
        Long id,
        Long produtoId,
        String descricao,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal) {

    public static ItemResponse de(ItemComanda i) {
        return new ItemResponse(
                i.getId(),
                i.getProduto().getId(),
                i.getProduto().getDescricao(),
                i.getQuantidade(),
                i.getValorUnitario(),
                i.getValorUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())));
    }
}
