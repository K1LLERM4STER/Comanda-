package com.comanda.comanda.service;

import java.util.List;
import com.comanda.comanda.dto.request.EstabelecimentoCadastroRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comanda.comanda.Model.Estabelecimento;
import com.comanda.comanda.dto.request.EstabelecimentoAtualizacaoRequest;
import com.comanda.comanda.exception.ConflitoException;
import com.comanda.comanda.exception.RecursoNaoEncontradoException;
import com.comanda.comanda.repository.EstabelecimentoRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EstabelecimentoService {

    private final EstabelecimentoRepository repository;

    @Transactional
    public Estabelecimento salvarEstabelecimento(EstabelecimentoCadastroRequest e) {
        if (repository.existsByCnpj(e.cnpj())) {
            throw new ConflitoException("CNPJ já cadastrado.");
        }
        // Objeto novo (ativo = true no construtor): um "id" enviado no JSON é ignorado
        return repository.save(new Estabelecimento(e.nome(), e.cnpj(), e.sobre(), e.endereco(), e.email()));
    }

    @Transactional(readOnly = true)
    public Estabelecimento buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Estabelecimento> listarEstabelecimentos() {
        return repository.findAll();
    }

    @Transactional
    public Estabelecimento atualizarEstabelecimento(Long id, EstabelecimentoAtualizacaoRequest novosDados) {
        Estabelecimento estabelecimento = buscarPorId(id);
        estabelecimento.setNome(novosDados.nome());
        estabelecimento.setEndereco(novosDados.endereco());
        estabelecimento.setSobre(novosDados.sobre());
        estabelecimento.setEmail(novosDados.email());
        return repository.save(estabelecimento);
    }

    @Transactional
    public void desativarEstabelecimento(Long id) {
        Estabelecimento estabelecimento = buscarPorId(id);
        if (!Boolean.TRUE.equals(estabelecimento.getAtivo())) {
            throw new ConflitoException("O estabelecimento já está desativado.");
        }
        estabelecimento.setAtivo(false);
        repository.save(estabelecimento);
    }

    @Transactional
    public void reativarEstabelecimento(Long id) {
        Estabelecimento estabelecimento = buscarPorId(id);
        if (Boolean.TRUE.equals(estabelecimento.getAtivo())) {
            throw new ConflitoException("O estabelecimento já está ativo.");
        }
        estabelecimento.setAtivo(true);
        repository.save(estabelecimento);
    }
}
