package com.comanda.comanda.dto.response;

import com.comanda.comanda.Model.Estabelecimento;


public record EstabelecimentoResponse(Long id, String nome, String cnpj, String sobre, String endereco, String email, Boolean ativo) {
    public static EstabelecimentoResponse de(Estabelecimento e) {
        return new EstabelecimentoResponse(e.getId(), e.getNome(), e.getCnpj(), e.getSobre(), e.getEndereco(), e.getEmail(), e.getAtivo());
    }
}
