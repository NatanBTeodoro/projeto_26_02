package com.projeto.service;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.entity.Usuario;
import com.projeto.repository.UsuarioRepository;
import com.projeto.seguranca.PoliticaSenha;
import com.projeto.util.exception.RequisicaoInvalidaException;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Service
public class RecuperacaoSenhaService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final ObjectProvider<JavaMailSender> mailSender;
	private final boolean exporToken;
	private final String mailHost;
	private final Map<String, Pendente> pendentes = new ConcurrentHashMap<>();

	public RecuperacaoSenhaService(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			ObjectProvider<JavaMailSender> mailSender,
			@Value("${app.recuperacao.expor-token:true}") boolean exporToken,
			@Value("${spring.mail.host:}") String mailHost) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.mailSender = mailSender;
		this.exporToken = exporToken;
		this.mailHost = mailHost;
	}

	public RecuperacaoResponse solicitar(Pedido pedido) {
		String email = pedido.email().trim().toLowerCase(Locale.ROOT);
		String token = UUID.randomUUID().toString();
		usuarioRepository.findByEmailIgnoreCase(email)
				.filter(Usuario::isAtivo)
				.ifPresent(usuario -> pendentes.put(token, new Pendente(email, Instant.now().plusSeconds(30 * 60))));
		limparVencidos();
		boolean enviado = enviar(email, token);
		String tokenVisivel = exporToken && pendentes.containsKey(token) ? token : null;
		String mensagem = enviado
				? "Se o e-mail existir, as instruções foram enviadas."
				: "SMTP não configurado. Se o e-mail existir, use o token devolvido para redefinir a senha no Postman.";
		return new RecuperacaoResponse(mensagem, tokenVisivel);
	}

	@Transactional
	public void redefinir(Redefinicao pedido) {
		PoliticaSenha.validar(pedido.senha());
		Pendente pendente = pendentes.remove(pedido.token());
		if (pendente == null || pendente.expira().isBefore(Instant.now())) {
			throw new RequisicaoInvalidaException("Token inválido ou expirado");
		}
		String email = pedido.email().trim().toLowerCase(Locale.ROOT);
		if (!pendente.email().equals(email)) {
			throw new RequisicaoInvalidaException("Token inválido ou expirado");
		}
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
				.filter(Usuario::isAtivo)
				.orElseThrow(() -> new RequisicaoInvalidaException("Token inválido ou expirado"));
		usuario.setSenhaHash(passwordEncoder.encode(pedido.senha()));
		usuarioRepository.save(usuario);
	}

	private boolean enviar(String email, String token) {
		if (mailHost == null || mailHost.isBlank()) {
			return false;
		}
		JavaMailSender enviador = mailSender.getIfAvailable();
		if (enviador == null || !pendentes.containsKey(token)) {
			return false;
		}
		try {
			SimpleMailMessage mensagem = new SimpleMailMessage();
			mensagem.setTo(email);
			mensagem.setSubject("Redefinição de senha");
			mensagem.setText("Use este token em POST /api/auth/redefinir-senha: " + token);
			enviador.send(mensagem);
			return true;
		} catch (RuntimeException ex) {
			return false;
		}
	}

	private void limparVencidos() {
		Instant agora = Instant.now();
		pendentes.entrySet().removeIf(item -> item.getValue().expira().isBefore(agora));
	}

	public record Pedido(@NotBlank @Email @Size(max = 255) String email) {
	}

	public record Redefinicao(
			@NotBlank @Email String email,
			@NotBlank String token,
			@NotBlank @Size(min = 8, max = 72) String senha) {
	}

	public record RecuperacaoResponse(String mensagem, String token) {
	}

	private record Pendente(String email, Instant expira) {
	}
}
