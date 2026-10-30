package com.comanda.comanda.service;

import java.util.List;
import com.comanda.comanda.dto.request.MesaCadastroRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comanda.comanda.Model.Estabelecimento;
import com.comanda.comanda.Model.Mesa;
import com.comanda.comanda.dto.request.MesaAtualizacaoRequest;
import com.comanda.comanda.exception.ConflitoException;
import com.comanda.comanda.exception.RecursoNaoEncontradoException;
import com.comanda.comanda.exception.RegraNegocioException;
import com.comanda.comanda.repository.EstabelecimentoRepository;
import com.comanda.comanda.repository.MesaRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MesaService {

    private final MesaRepository repository;
    private final EstabelecimentoRepository estabelecimentoRepository;

    @Transactional
    public Mesa salvarMesa(MesaCadastroRequest m) {
        if (m.estabelecimentoId() == null) {
            throw new RegraNegocioException("Informe o estabelecimento da mesa (estabelecimentoId).");
        }
        Estabelecimento estab = estabelecimentoRepository.findById(m.estabelecimentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento não encontrado."));
        if (repository.existsByEstabelecimentoIdAndNumeroMesa(estab.getId(), m.numeroMesa())) {
            throw new ConflitoException("Já existe uma mesa com esse número neste estabelecimento.");
        }
        return repository.save(new Mesa(estab, m.numeroMesa()));
    }

    @Transactional(readOnly = true)
    public Mesa buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mesa não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<Mesa> listarMesas() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Mesa> listarPorEstabelecimento(Long estabelecimentoId) {
        return repository.findByEstabelecimentoId(estabelecimentoId);
    }

    @Transactional
    public Mesa atualizarMesa(Long id, MesaAtualizacaoRequest novosDados) {
        Mesa mesa = buscarPorId(id);
        if (!mesa.getNumeroMesa().equals(novosDados.numeroMesa())
                && repository.existsByEstabelecimentoIdAndNumeroMesa(mesa.getEstabelecimento().getId(), novosDados.numeroMesa())) {
            throw new ConflitoException("Já existe uma mesa com esse número neste estabelecimento.");
        }
        mesa.setNumeroMesa(novosDados.numeroMesa());
        return repository.save(mesa);
    }

    @Transactional
    public void deletarMesa(Long id) {
        repository.delete(buscarPorId(id));
    }
}
