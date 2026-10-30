package com.comanda.comanda.dto.response;

import com.comanda.comanda.Model.Grupo;

public record GrupoResponse(Long id, String descricao, Long estabelecimentoId) {
    public static GrupoResponse de(Grupo e) {
        return new GrupoResponse(e.getId(), e.getDescricao(), e.getEstabelecimento().getId());
    }
}
