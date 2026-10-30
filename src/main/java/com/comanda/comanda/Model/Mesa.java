package com.comanda.comanda.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_mesa_estabelecimento_numero", columnNames = {"estabelecimento_id", "numero_mesa"}))
@Getter
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "estabelecimento_id", nullable = false)
    private Estabelecimento estabelecimento;

    @Setter
    @NotBlank
    @Size(max = 255)
    private String numeroMesa;

    public Mesa(Estabelecimento estabelecimento, String numeroMesa) {
        this.estabelecimento = estabelecimento;
        this.numeroMesa = numeroMesa;
    }
}
