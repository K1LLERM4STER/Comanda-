package com.comanda.comanda.dto.response;

import com.comanda.comanda.Model.Cliente;

public record ClienteResponse(Long id, String nome, String email, String cpf) {
    public static ClienteResponse de(Cliente e) {
        return new ClienteResponse(e.getId(), e.getNome(), e.getEmail(), e.getCpf());
    }
}
