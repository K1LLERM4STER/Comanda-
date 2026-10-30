package com.comanda.comanda.Model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class ItemComanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnore
    @JoinColumn(name = "comanda_id", nullable = false)
    @NotNull
    private Comanda comanda;

    @ManyToOne(optional = false)
    @NotNull
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Setter
    @NotNull
    @Positive
    private Integer quantidade;

    @NotNull
    @Positive
    @Digits(integer = 8, fraction = 2)
    @Column(precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    public ItemComanda(Produto produto, Integer quantidade, BigDecimal valorUnitario, Comanda comanda) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.comanda = comanda;
    }
}
