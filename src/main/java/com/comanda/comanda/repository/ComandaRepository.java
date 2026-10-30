package com.comanda.comanda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comanda.comanda.Model.Comanda;
import com.comanda.comanda.Model.StatusComanda;

import jakarta.persistence.LockModeType;

public interface ComandaRepository extends JpaRepository<Comanda, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Comanda c where c.id = :id")
    Optional<Comanda> buscarComTrava(@Param("id") Long id);

    List<Comanda> findByEstabelecimentoId(Long estabelecimentoId);

    List<Comanda> findByStatus(StatusComanda status);

    List<Comanda> findByEstabelecimentoIdAndStatus(Long estabelecimentoId, StatusComanda status);

    boolean existsByMesaIdAndStatus(Long mesaId, StatusComanda status);
}
