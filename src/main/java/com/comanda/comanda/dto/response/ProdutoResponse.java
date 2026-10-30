package com.comanda.comanda.dto.response;

import com.comanda.comanda.Model.Produto;
import java.math.BigDecimal;

public record ProdutoResponse(Long id, String descricao, String unidade, Long estoque, BigDecimal pvenda, Long grupoId, String grupoDescricao, Long estabelecimentoId) {
    public static ProdutoResponse de(Produto e) {
        return new ProdutoResponse(e.getId(), e.getDescricao(), e.getUnidade(), e.getEstoque(), e.getPvenda(), e.getGrupo().getId(), e.getGrupo().getDescricao(), e.getEstabelecimento().getId());
    }
}
