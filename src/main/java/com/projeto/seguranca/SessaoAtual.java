package com.projeto.seguranca;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.projeto.util.exception.NaoAutenticadoException;

@Component
public class SessaoAtual {

	public UsuarioAtual exigir() {
		Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacao == null || !(autenticacao.getPrincipal() instanceof UsuarioDetails detalhes)) {
			throw new NaoAutenticadoException("Autenticação necessária");
		}
		return detalhes.atual();
	}
}
