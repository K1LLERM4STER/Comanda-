package com.comanda.comanda.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.comanda.comanda.Model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);
}
