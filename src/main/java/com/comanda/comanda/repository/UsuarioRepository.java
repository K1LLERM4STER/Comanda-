package com.comanda.comanda.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.comanda.comanda.Model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);
}
