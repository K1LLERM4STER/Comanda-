package com.comanda.comanda.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.comanda.comanda.Model.Estabelecimento;

public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {

    boolean existsByCnpj(String cnpj);
}
