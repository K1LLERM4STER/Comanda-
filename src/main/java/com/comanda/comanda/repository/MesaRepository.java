package com.comanda.comanda.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.comanda.comanda.Model.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findByEstabelecimentoId(Long estabelecimentoId);

    boolean existsByEstabelecimentoIdAndNumeroMesa(Long estabelecimentoId, String numeroMesa);
}
