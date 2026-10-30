package com.comanda.comanda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comanda.comanda.Model.Produto;

import jakarta.persistence.LockModeType;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByGrupoId(Long id);

    // Trava a linha do produto enquanto o estoque e alterado (evita vender estoque que nao existe)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> buscarComTrava(@Param("id") Long id);

}
