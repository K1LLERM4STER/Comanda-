package com.comanda.comanda.dto.response;

import com.comanda.comanda.Model.Usuario;

public record UsuarioResponse(Long id, String nome, String email, Long estabelecimentoId) {
    public static UsuarioResponse de(Usuario e) {
        return new UsuarioResponse(e.getId(), e.getNome(), e.getEmail(), e.getEstabelecimento().getId());
    }
}
