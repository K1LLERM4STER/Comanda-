package com.comanda.comanda.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.comanda.comanda.Model.Comanda;
import com.comanda.comanda.Model.StatusComanda;

public record ComandaResponse(
        Long id,
        Long estabelecimentoId,
        Long mesaId,
        String numeroMesa,
        Long usuarioId,
        String usuarioNome,
        Long clienteId,
        String clienteNome,
        StatusComanda status,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        List<ItemResponse> itens,
        BigDecimal total) {

    public static ComandaResponse de(Comanda c) {
        return new ComandaResponse(
                c.getId(),
                c.getEstabelecimento().getId(),
                c.getMesa().getId(),
                c.getMesa().getNumeroMesa(),
                c.getUsuario().getId(),
                c.getUsuario().getNome(),
                c.getCliente() != null ? c.getCliente().getId() : null,
                c.getCliente() != null ? c.getCliente().getNome() : null,
                c.getStatus(),
                c.getDataAbertura(),
                c.getDataFechamento(),
                c.getItens().stream().map(ItemResponse::de).toList(),
                c.calcularTotal());
    }
}
