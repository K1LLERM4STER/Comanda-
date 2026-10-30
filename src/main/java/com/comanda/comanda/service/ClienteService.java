package com.comanda.comanda.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import com.comanda.comanda.dto.request.ClienteCadastroRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comanda.comanda.Model.Cliente;
import com.comanda.comanda.dto.request.ClienteAtualizacaoRequest;
import com.comanda.comanda.exception.ConflitoException;
import com.comanda.comanda.exception.RegraNegocioException;
import com.comanda.comanda.exception.RecursoNaoEncontradoException;
import com.comanda.comanda.repository.ClienteRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Cliente salvarCliente(ClienteCadastroRequest c) {
        if (c.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RegraNegocioException("A senha deve ter no máximo 72 bytes em UTF-8.");
        }
        if (repository.existsByEmail(c.email())) {
            throw new ConflitoException("E-mail já cadastrado.");
        }
        if (repository.existsByCpf(c.cpf())) {
            throw new ConflitoException("CPF já cadastrado.");
        }
        return repository.save(new Cliente(c.nome(), c.email(), passwordEncoder.encode(c.senha()), c.cpf()));
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {
        return repository.findAll();
    }

    @Transactional
    public Cliente atualizarCliente(Long id, ClienteAtualizacaoRequest novosDados) {
        Cliente cliente = buscarPorId(id);
        cliente.setNome(novosDados.nome());
        cliente.setEmail(novosDados.email());
        cliente.setCpf(novosDados.cpf());
        return repository.save(cliente);
    }

    @Transactional
    public void deletarCliente(Long id) {
        repository.delete(buscarPorId(id));
    }
}
