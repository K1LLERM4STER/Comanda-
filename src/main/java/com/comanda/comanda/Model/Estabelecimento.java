package com.comanda.comanda.Model;

import org.hibernate.validator.constraints.br.CNPJ;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = {"cnpj"})})
public class Estabelecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotBlank
    @Size(max = 255)
    private String nome;

    @CNPJ
    @NotBlank
    @Size(max = 255)
    private String cnpj;

    @Setter
    @Size(max = 255)
    private String sobre;

    @Setter
    @NotBlank
    @Size(max = 255)
    private String endereco;

    @Email
    @Setter
    @NotBlank
    @Size(max = 255)
    private String email;

    @Setter
    private Boolean ativo;

    public Estabelecimento(String nome, String cnpj, String sobre, String endereco, String email) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.sobre = sobre;
        this.endereco = endereco;
        this.email = email;
        this.ativo = true;
    }
}
