package com.comanda.comanda.service;

import java.util.List;
import com.comanda.comanda.dto.request.GrupoCadastroRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comanda.comanda.Model.Estabelecimento;
import com.comanda.comanda.Model.Grupo;
import com.comanda.comanda.dto.request.GrupoAtualizacaoRequest;
import com.comanda.comanda.exception.RecursoNaoEncontradoException;
import com.comanda.comanda.exception.RegraNegocioException;
import com.comanda.comanda.repository.EstabelecimentoRepository;
import com.comanda.comanda.repository.GrupoRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GrupoService {

    private final GrupoRepository repository;
    private final EstabelecimentoRepository estabelecimentoRepository;

    @Transactional
    public Grupo salvarGrupo(GrupoCadastroRequest g) {
        if (g.estabelecimentoId() == null) {
            throw new RegraNegocioException("Informe o estabelecimento do grupo (estabelecimentoId).");
        }
        Estabelecimento estab = estabelecimentoRepository.findById(g.estabelecimentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento não encontrado."));
        return repository.save(new Grupo(g.descricao(), estab));
    }

    @Transactional(readOnly = true)
    public Grupo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grupo não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Grupo> listarGrupos() {
        return repository.findAll();
    }

    @Transactional
    public Grupo atualizarGrupo(Long id, GrupoAtualizacaoRequest novosDados) {
        Grupo grupo = buscarPorId(id);
        grupo.setDescricao(novosDados.descricao());
        return repository.save(grupo);
    }

    @Transactional
    public void deletarGrupo(Long id) {
        repository.delete(buscarPorId(id));
    }
}
