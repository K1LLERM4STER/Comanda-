package com.comanda.comanda.service;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comanda.comanda.Model.Cliente;
import com.comanda.comanda.Model.Comanda;
import com.comanda.comanda.Model.ItemComanda;
import com.comanda.comanda.Model.Mesa;
import com.comanda.comanda.Model.Produto;
import com.comanda.comanda.Model.StatusComanda;
import com.comanda.comanda.Model.Usuario;
import com.comanda.comanda.dto.request.ComandaRequest;
import com.comanda.comanda.dto.response.ComandaResponse;
import com.comanda.comanda.dto.request.ItemRequest;
import com.comanda.comanda.exception.ConflitoException;
import com.comanda.comanda.exception.RecursoNaoEncontradoException;
import com.comanda.comanda.exception.RegraNegocioException;
import com.comanda.comanda.repository.ClienteRepository;
import com.comanda.comanda.repository.ComandaRepository;
import com.comanda.comanda.repository.MesaRepository;
import com.comanda.comanda.repository.ProdutoRepository;
import com.comanda.comanda.repository.UsuarioRepository;

import lombok.AllArgsConstructor;
import jakarta.persistence.EntityManager;

@Service
@AllArgsConstructor
public class ComandaService {

    private final ComandaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final MesaRepository mesaRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final EntityManager entityManager;

    @Transactional
    public ComandaResponse abrirComanda(ComandaRequest req) {
        Mesa mesa = mesaRepository.findById(req.mesaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mesa não encontrada."));
        Usuario usuario = usuarioRepository.findById(req.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        if (!usuario.getEstabelecimento().getId().equals(mesa.getEstabelecimento().getId())) {
            throw new RegraNegocioException("Usuário e mesa pertencem a estabelecimentos diferentes.");
        }
        if (Boolean.FALSE.equals(mesa.getEstabelecimento().getAtivo())) {
            throw new RegraNegocioException("Estabelecimento desativado.");
        }
        if (repository.existsByMesaIdAndStatus(mesa.getId(), StatusComanda.ABERTA)) {
            throw new ConflitoException("Já existe uma comanda aberta para esta mesa.");
        }

        Cliente cliente = null;
        if (req.clienteId() != null) {
            cliente = clienteRepository.findById(req.clienteId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
        }

        Comanda comanda = new Comanda(usuario, mesa.getEstabelecimento(), mesa, cliente);
        return ComandaResponse.de(repository.save(comanda));
    }

    @Transactional(readOnly = true)
    public ComandaResponse buscarPorId(Long id) {
        return ComandaResponse.de(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ComandaResponse> listar(Long estabelecimentoId, StatusComanda status) {
        List<Comanda> comandas;
        if (estabelecimentoId != null && status != null) {
            comandas = repository.findByEstabelecimentoIdAndStatus(estabelecimentoId, status);
        } else if (estabelecimentoId != null) {
            comandas = repository.findByEstabelecimentoId(estabelecimentoId);
        } else if (status != null) {
            comandas = repository.findByStatus(status);
        } else {
            comandas = repository.findAll();
        }
        return comandas.stream().map(ComandaResponse::de).toList();
    }

    @Transactional
    public ComandaResponse adicionarItem(Long comandaId, ItemRequest req) {
        Comanda comanda = buscarEntidadeComTrava(comandaId);
        garantirAberta(comanda);

        Produto produto = buscarProdutoComTravaAtualizado(req.produtoId());

        if (!produto.getEstabelecimento().getId().equals(comanda.getEstabelecimento().getId())) {
            throw new RegraNegocioException("Produto não pertence ao estabelecimento da comanda.");
        }
        if (produto.getEstoque() < req.quantidade()) {
            throw new RegraNegocioException("Estoque insuficiente. Disponível: " + produto.getEstoque() + ".");
        }

        // Se o mesmo produto (pelo mesmo preço) ja esta na comanda, soma a quantidade
        ItemComanda existente = comanda.getItens().stream()
                .filter(i -> i.getProduto().getId().equals(produto.getId())
                        && i.getValorUnitario().compareTo(produto.getPvenda()) == 0)
                .findFirst()
                .orElse(null);

        if (existente != null) {
            try {
                existente.setQuantidade(Math.addExact(existente.getQuantidade(), req.quantidade()));
            } catch (ArithmeticException e) {
                throw new RegraNegocioException("A quantidade ultrapassa o limite permitido por item.");
            }
        } else {
            comanda.adicionarItem(new ItemComanda(produto, req.quantidade(), produto.getPvenda(), comanda));
        }

        produto.setEstoque(produto.getEstoque() - req.quantidade());
        return ComandaResponse.de(repository.saveAndFlush(comanda));
    }

    @Transactional
    public ComandaResponse removerItem(Long comandaId, Long itemId) {
        Comanda comanda = buscarEntidadeComTrava(comandaId);
        garantirAberta(comanda);

        ItemComanda item = comanda.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado nesta comanda."));

        devolverEstoque(item.getProduto().getId(), item.getQuantidade());
        comanda.removerItem(item);

        return ComandaResponse.de(repository.saveAndFlush(comanda));
    }

    @Transactional
    public ComandaResponse fecharComanda(Long id) {
        Comanda comanda = buscarEntidadeComTrava(id);
        garantirAberta(comanda);
        comanda.fechar();
        return ComandaResponse.de(repository.save(comanda));
    }

    @Transactional
    public ComandaResponse cancelarComanda(Long id) {
        Comanda comanda = buscarEntidadeComTrava(id);
        garantirAberta(comanda);
        // Comanda primeiro; produtos distintos em ordem de ID para evitar ciclos de travas.
        // Agrupar tambem preserva devolucoes de itens do mesmo produto com precos diferentes.
        comanda.getItens().stream()
                .collect(Collectors.groupingBy(i -> i.getProduto().getId(), TreeMap::new,
                        Collectors.summingLong(item -> item.getQuantidade().longValue())))
                .forEach((produtoId, quantidade) -> devolverEstoque(produtoId, quantidade.longValue()));
        comanda.cancelar();
        return ComandaResponse.de(repository.save(comanda));
    }

    private Produto buscarProdutoComTravaAtualizado(Long id) {
        Produto produto = produtoRepository.buscarComTrava(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
        // A consulta com trava nao atualiza uma entidade ja carregada no contexto JPA.
        // Recarregar sob a trava impede usar estoque anterior a outra transacao.
        entityManager.refresh(produto);
        return produto;
    }

    private void devolverEstoque(Long produtoId, long quantidade) {
        Produto produto = buscarProdutoComTravaAtualizado(produtoId);
        try {
            produto.setEstoque(Math.addExact(produto.getEstoque(), quantidade));
        } catch (ArithmeticException e) {
            throw new RegraNegocioException("A devolução ultrapassa o limite de estoque permitido.");
        }
    }

    private Comanda buscarEntidadeComTrava(Long id) {
        Comanda comanda = repository.buscarComTrava(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comanda não encontrada."));
        // Garante estado atual mesmo se o provedor adquirir a trava em uma consulta posterior.
        entityManager.refresh(comanda);
        return comanda;
    }

    private Comanda buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comanda não encontrada."));
    }

    private void garantirAberta(Comanda comanda) {
        if (!comanda.estaAberta()) {
            throw new RegraNegocioException("A comanda está " + comanda.getStatus() + " e não pode ser alterada.");
        }
    }
}
