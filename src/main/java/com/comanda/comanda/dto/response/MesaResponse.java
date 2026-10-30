package com.comanda.comanda.dto.response;

import com.comanda.comanda.Model.Mesa;

public record MesaResponse(Long id, String numeroMesa, Long estabelecimentoId) {
    public static MesaResponse de(Mesa e) {
        return new MesaResponse(e.getId(), e.getNumeroMesa(), e.getEstabelecimento().getId());
    }
}
