package com.projeto.service;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.UsuarioDTO;
import com.projeto.entity.Estabelecimento;
import com.projeto.entity.Usuario;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.repository.UsuarioRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.PoliticaSenha;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.RecursoJaExistenteException;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RegraDeNegocioException;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final EstabelecimentoRepository estabelecimentoRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(
			UsuarioRepository usuarioRepository,
			EstabelecimentoRepository estabelecimentoRepository,
			PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.estabelecimentoRepository = estabelecimentoRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Page<UsuarioDTO.Response> listar(Pageable pageable, UsuarioAtual sessao) {
		Acesso.exigirAdministrador(sessao);
		return usuarioRepository.findByEstabelecimentoId(sessao.estabelecimentoId(), pageable)
				.map(UsuarioDTO.Response::from);
	}

	public UsuarioDTO.Response buscar(Long id, UsuarioAtual sessao) {
		Acesso.exigirAdministrador(sessao);
		return UsuarioDTO.Response.from(carregar(id, sessao));
	}

	@Transactional
	public UsuarioDTO.Response criar(UsuarioDTO.Create dto, UsuarioAtual sessao) {
		Acesso.exigirAdministrador(sessao);
		Acesso.exigirMesmoEstabelecimento(dto.estabelecimentoId(), sessao);
		String email = normalizar(dto.email());
		PoliticaSenha.validar(dto.senha());
		if (usuarioRepository.existsByEmailIgnoreCase(email)) {
			throw new RecursoJaExistenteException("E-mail já cadastrado");
		}
		Estabelecimento estabelecimento = estabelecimentoRepository.findById(sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", sessao.estabelecimentoId()));
		Usuario usuario = new Usuario();
		usuario.setEstabelecimento(estabelecimento);
		usuario.setEmail(email);
		usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));
		usuario.setPerfil(dto.perfil());
		usuario.setAtivo(true);
		Usuario salvo = usuarioRepository.save(usuario);
		if (salvo.getSenhaHash().equals(dto.senha())) {
			throw new IllegalStateException("Senha não pode ser persistida em texto");
		}
		return UsuarioDTO.Response.from(salvo);
	}

	@Transactional
	public UsuarioDTO.Response atualizar(Long id, UsuarioDTO.Update dto, UsuarioAtual sessao) {
		Acesso.exigirAdministrador(sessao);
		Usuario usuario = carregar(id, sessao);
		if (dto.email() != null && !dto.email().isBlank()) {
			String email = normalizar(dto.email());
			usuarioRepository.findByEmailIgnoreCase(email)
					.filter(outro -> !outro.getId().equals(usuario.getId()))
					.ifPresent(outro -> {
						throw new RecursoJaExistenteException("E-mail já cadastrado");
					});
			usuario.setEmail(email);
		}
		if (dto.senha() != null && !dto.senha().isBlank()) {
			PoliticaSenha.validar(dto.senha());
			usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));
		}
		if (dto.perfil() != null) {
			usuario.setPerfil(dto.perfil());
		}
		if (dto.ativo() != null) {
			if (!dto.ativo() && usuario.getId().equals(sessao.id())) {
				throw new RegraDeNegocioException("Não é possível inativar o próprio usuário");
			}
			usuario.setAtivo(dto.ativo());
		}
		return UsuarioDTO.Response.from(usuarioRepository.save(usuario));
	}

	private Usuario carregar(Long id, UsuarioAtual sessao) {
		return usuarioRepository.findByIdAndEstabelecimentoId(id, sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
	}

	private String normalizar(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
