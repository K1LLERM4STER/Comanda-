package com.comanda.comanda.service;

import java.util.List;
import com.comanda.comanda.dto.request.ProdutoCadastroRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comanda.comanda.Model.Estabelecimento;
import com.comanda.comanda.Model.Grupo;
import com.comanda.comanda.Model.Produto;
import com.comanda.comanda.dto.request.ProdutoAtualizacaoRequest;
import com.comanda.comanda.dto.request.AjusteEstoqueRequest;
import com.comanda.comanda.exception.RecursoNaoEncontradoException;
import com.comanda.comanda.exception.RegraNegocioException;
import com.comanda.comanda.repository.EstabelecimentoRepository;
import com.comanda.comanda.repository.GrupoRepository;
import com.comanda.comanda.repository.ProdutoRepository;

import lombok.AllArgsConstructor;
import jakarta.persistence.EntityManager;

@Service
@AllArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;
    private final GrupoRepository grupoRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final EntityManager entityManager;

    @Transactional
    public Produto salvarProduto(ProdutoCadastroRequest p) {
        if (p.grupoId() == null
                || p.estabelecimentoId() == null) {
            throw new RegraNegocioException("Informe grupoId e estabelecimentoId do produto.");
        }
        Grupo grupo = grupoRepository.findById(p.grupoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grupo não encontrado."));
        Estabelecimento estab = estabelecimentoRepository.findById(p.estabelecimentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento não encontrado."));
        if (!grupo.getEstabelecimento().getId().equals(estab.getId())) {
            throw new RegraNegocioException("O grupo não pertence a esse estabelecimento.");
        }
        // Cria um objeto novo: assim um "id" enviado no JSON é ignorado
        return repository.save(new Produto(grupo, p.descricao(), p.estoque(), p.pvenda(), p.unidade(), estab));
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Produto> listarProdutos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Produto> buscarPorGrupoId(Long grupoId) {
        if (!grupoRepository.existsById(grupoId)) {
            throw new RecursoNaoEncontradoException("Grupo não encontrado.");
        }
        return repository.findByGrupoId(grupoId);
    }

    @Transactional
    public Produto atualizarProduto(Long id, ProdutoAtualizacaoRequest novosDados) {
        Produto produto = buscarProdutoComTravaAtualizado(id);

        if (novosDados.grupoId() == null) {
            throw new RegraNegocioException("Informe o grupo do produto (grupoId).");
        }
        Grupo grupo = grupoRepository.findById(novosDados.grupoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grupo não encontrado."));
        if (!grupo.getEstabelecimento().getId().equals(produto.getEstabelecimento().getId())) {
            throw new RegraNegocioException("O grupo não pertence ao estabelecimento do produto.");
        }

        produto.setDescricao(novosDados.descricao());
        produto.setUnidade(novosDados.unidade());
        produto.setPvenda(novosDados.pvenda());
        produto.setGrupo(grupo);
        return repository.save(produto);
    }

    @Transactional
    public Produto ajustarEstoque(Long id, AjusteEstoqueRequest request) {
        if (request.variacao() == null || request.variacao() == 0) {
            throw new RegraNegocioException("Informe uma variação de estoque diferente de zero.");
        }
        Produto produto = buscarProdutoComTravaAtualizado(id);
        long novoEstoque;
        try {
            novoEstoque = Math.addExact(produto.getEstoque(), request.variacao());
        } catch (ArithmeticException e) {
            throw new RegraNegocioException("O ajuste ultrapassa o limite de estoque permitido.");
        }
        if (novoEstoque < 0) {
            throw new RegraNegocioException("O ajuste não pode deixar o estoque negativo.");
        }
        produto.setEstoque(novoEstoque);
        return repository.save(produto);
    }

    private Produto buscarProdutoComTravaAtualizado(Long id) {
        Produto produto = repository.buscarComTrava(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
        // Evita gravar saldo antigo, inclusive na edicao cadastral, que nao altera estoque.
        entityManager.refresh(produto);
        return produto;
    }

    @Transactional
    public void deletarProduto(Long id) {
        repository.delete(buscarPorId(id));
    }
}
