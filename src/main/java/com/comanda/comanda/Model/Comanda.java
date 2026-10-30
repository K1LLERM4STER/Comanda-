package com.comanda.comanda.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @NotNull
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @NotNull
    @JoinColumn(name = "estabelecimento_id", nullable = false)
    private Estabelecimento estabelecimento;

    @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<ItemComanda> itens = new ArrayList<>();

    @ManyToOne(optional = false)
    @NotNull
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusComanda status;

    private LocalDateTime dataAbertura;

    private LocalDateTime dataFechamento;

    public Comanda(Usuario usuario, Estabelecimento estabelecimento, Mesa mesa, Cliente cliente) {
        this.usuario = usuario;
        this.estabelecimento = estabelecimento;
        this.mesa = mesa;
        this.cliente = cliente;
        this.status = StatusComanda.ABERTA;
        this.dataAbertura = LocalDateTime.now();
    }

    public boolean estaAberta() {
        return status == StatusComanda.ABERTA;
    }

    public List<ItemComanda> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public void adicionarItem(ItemComanda item) {
        Objects.requireNonNull(item, "O item deve ser informado.");
        if (item.getComanda() != this) {
            throw new IllegalArgumentException("O item deve pertencer a esta comanda.");
        }
        if (!itens.contains(item)) {
            itens.add(item);
        }
    }

    public void removerItem(ItemComanda item) {
        itens.remove(item);
    }

    public BigDecimal calcularTotal() {
        return itens.stream()
                .map(i -> i.getValorUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, (total, subtotal) -> total.add(subtotal));
    }

    public void fechar() {
        this.status = StatusComanda.FECHADA;
        this.dataFechamento = LocalDateTime.now();
    }

    public void cancelar() {
        this.status = StatusComanda.CANCELADA;
        this.dataFechamento = LocalDateTime.now();
    }
}
