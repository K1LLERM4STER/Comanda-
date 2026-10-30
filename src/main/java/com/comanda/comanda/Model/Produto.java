package com.comanda.comanda.Model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@NoArgsConstructor
@Entity
@ToString(onlyExplicitlyIncluded = true)
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ToString.Include
    @Setter
    @NotBlank
    @Size(max = 255)
    private String unidade;

    @ToString.Include
    @Setter
    @NotBlank
    @Size(max = 255)
    private String descricao;

    @ToString.Include
    @Setter
    @PositiveOrZero
    @NotNull
    private Long estoque;

    @ToString.Include
    @Setter
    @Positive
    @NotNull
    @Digits(integer = 8, fraction = 2)
    @Column(precision = 10, scale = 2)
    private BigDecimal pvenda;

    @JoinColumn(name = "estabelecimento_id", nullable = false)
    @NotNull
    @ManyToOne(optional = false)
    private Estabelecimento estabelecimento;

    public Produto(Grupo grupo, String descricao, Long estoque, BigDecimal pvenda, String unidade, Estabelecimento estabelecimento) {
        this.grupo = grupo;
        this.descricao = descricao;
        this.estoque = estoque;
        this.pvenda = pvenda;
        this.unidade = unidade;
        this.estabelecimento = estabelecimento;
    }
}
