package com.projeto.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.UsuarioDTO;
import com.projeto.entity.Usuario;
import com.projeto.entity.enums.Perfil;
import com.projeto.repository.UsuarioRepository;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RegraDeNegocioException;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final SessaoAtual sessaoAtual;

	public UsuarioService(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			SessaoAtual sessaoAtual) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.sessaoAtual = sessaoAtual;
	}

	public List<UsuarioDTO.Response> listar() {
		return usuarioRepository.findAllByOrderByEmailAsc().stream().map(UsuarioDTO.Response::de).toList();
	}

	@Transactional
	public UsuarioDTO.Response criar(UsuarioDTO.Create dto) {
		String email = normalizar(dto.email());
		if (usuarioRepository.existsByEmailIgnoreCase(email)) {
			throw new RegraDeNegocioException("E-mail já cadastrado");
		}
		exigirPerfil(dto.perfil());
		Usuario usuario = new Usuario();
		usuario.setEmail(email);
		usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));
		usuario.setPerfil(dto.perfil());
		usuario.setAtivo(true);
		return UsuarioDTO.Response.de(usuarioRepository.save(usuario));
	}

	@Transactional
	public UsuarioDTO.Response atualizar(Long id, UsuarioDTO.Update dto) {
		Usuario usuario = carregar(id);
		if (dto.email() != null && !dto.email().isBlank()) {
			String email = normalizar(dto.email());
			usuarioRepository.findByEmailIgnoreCase(email)
					.filter(outro -> !outro.getId().equals(usuario.getId()))
					.ifPresent(outro -> {
						throw new RegraDeNegocioException("E-mail já cadastrado");
					});
			usuario.setEmail(email);
		}
		if (dto.senha() != null && !dto.senha().isBlank()) {
			usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));
			usuario.setTokenAcesso(null);
		}
		if (dto.perfil() != null) {
			exigirPerfil(dto.perfil());
			usuario.setPerfil(dto.perfil());
		}
		if (dto.ativo() != null) {
			if (!dto.ativo() && usuario.getId().equals(sessaoAtual.exigir().id())) {
				throw new RegraDeNegocioException("Não é possível inativar o próprio usuário");
			}
			usuario.setAtivo(dto.ativo());
			if (!dto.ativo()) {
				usuario.setTokenAcesso(null);
			}
		}
		return UsuarioDTO.Response.de(usuarioRepository.save(usuario));
	}

	private Usuario carregar(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
	}

	private void exigirPerfil(Perfil perfil) {
		if (perfil != Perfil.ADMINISTRADOR && perfil != Perfil.GESTOR) {
			throw new RegraDeNegocioException("Perfil inválido");
		}
	}

	private String normalizar(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
