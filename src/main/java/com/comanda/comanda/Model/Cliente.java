package com.comanda.comanda.Model;

import org.hibernate.validator.constraints.br.CPF;

import com.fasterxml.jackson.annotation.JsonProperty;

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
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = {"cpf"}), @UniqueConstraint(columnNames = {"email"})})
public class Cliente {

    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ToString.Include
    @NotBlank
    @Size(max = 255)
    private String nome;

    @Setter
    @Email
    @NotBlank
    @ToString.Include
    @Size(max = 255)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    @Size(max = 72)
    private String senha;

    @Setter
    @CPF
    @NotBlank
    @Size(max = 255)
    private String cpf;

    public Cliente(String nome, String email, String senha, String cpf) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.cpf = cpf;
    }
}
