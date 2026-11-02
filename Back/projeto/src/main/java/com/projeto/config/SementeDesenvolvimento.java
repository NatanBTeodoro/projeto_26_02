package com.projeto.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.entity.Estabelecimento;
import com.projeto.entity.Usuario;
import com.projeto.entity.enums.Perfil;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.repository.UsuarioRepository;
import com.projeto.seguranca.PoliticaSenha;

@Component
@ConditionalOnProperty(name = "app.semente.habilitada", havingValue = "true")
public class SementeDesenvolvimento implements ApplicationRunner {

	private final UsuarioRepository usuarioRepository;
	private final EstabelecimentoRepository estabelecimentoRepository;
	private final PasswordEncoder passwordEncoder;
	private final String email;
	private final String senha;

	public SementeDesenvolvimento(
			UsuarioRepository usuarioRepository,
			EstabelecimentoRepository estabelecimentoRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.semente.email}") String email,
			@Value("${app.semente.senha}") String senha) {
		this.usuarioRepository = usuarioRepository;
		this.estabelecimentoRepository = estabelecimentoRepository;
		this.passwordEncoder = passwordEncoder;
		this.email = email;
		this.senha = senha;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (usuarioRepository.count() > 0) {
			return;
		}
		PoliticaSenha.validar(senha);
		Estabelecimento estabelecimento = new Estabelecimento();
		estabelecimento.setNome("Casa piloto");
		estabelecimento.setAtivo(true);
		estabelecimento = estabelecimentoRepository.save(estabelecimento);

		Usuario administrador = new Usuario();
		administrador.setEstabelecimento(estabelecimento);
		administrador.setEmail(email.trim().toLowerCase(Locale.ROOT));
		administrador.setSenhaHash(passwordEncoder.encode(senha));
		administrador.setPerfil(Perfil.ADMINISTRADOR);
		administrador.setAtivo(true);
		usuarioRepository.save(administrador);
	}
}
