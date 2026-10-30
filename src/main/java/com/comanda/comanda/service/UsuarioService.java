package com.comanda.comanda.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import com.comanda.comanda.dto.request.UsuarioCadastroRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comanda.comanda.Model.Estabelecimento;
import com.comanda.comanda.Model.Usuario;
import com.comanda.comanda.dto.request.UsuarioAtualizacaoRequest;
import com.comanda.comanda.exception.ConflitoException;
import com.comanda.comanda.exception.RecursoNaoEncontradoException;
import com.comanda.comanda.exception.RegraNegocioException;
import com.comanda.comanda.repository.EstabelecimentoRepository;
import com.comanda.comanda.repository.UsuarioRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario salvarUsuario(UsuarioCadastroRequest u) {
        if (u.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RegraNegocioException("A senha deve ter no máximo 72 bytes em UTF-8.");
        }
        if (u.estabelecimentoId() == null) {
            throw new RegraNegocioException("Informe o estabelecimento do usuário (estabelecimentoId).");
        }
        Estabelecimento estab = estabelecimentoRepository.findById(u.estabelecimentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento não encontrado."));
        if (repository.existsByEmail(u.email())) {
            throw new ConflitoException("E-mail já cadastrado.");
        }
        return repository.save(new Usuario(u.nome(), u.email(), passwordEncoder.encode(u.senha()), estab));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return repository.findAll();
    }

    @Transactional
    public Usuario atualizarUsuario(Long id, UsuarioAtualizacaoRequest novosDados) {
        Usuario usuario = buscarPorId(id);
        usuario.setNome(novosDados.nome());
        return repository.save(usuario);
    }

    @Transactional
    public void deletarUsuario(Long id) {
        repository.delete(buscarPorId(id));
    }
}
