package com.projeto.seguranca;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) {
		String normalizado = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
		return usuarioRepository.findByEmailIgnoreCase(normalizado)
				.map(UsuarioDetails::new)
				.orElseThrow(() -> new UsernameNotFoundException("credenciais"));
	}
}
