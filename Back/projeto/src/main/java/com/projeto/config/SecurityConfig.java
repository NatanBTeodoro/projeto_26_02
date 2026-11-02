package com.projeto.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.projeto.seguranca.JsonAuthHandlers;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.origens:http://localhost:5173}") String origens) {
		CorsConfiguration cors = new CorsConfiguration();
		for (String origem : origens.split(",")) {
			String limpa = origem.trim();
			if (!limpa.isEmpty()) {
				cors.addAllowedOrigin(limpa);
			}
		}
		cors.addAllowedHeader("*");
		cors.addAllowedMethod("*");
		cors.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cors);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JsonAuthHandlers jsonAuth,
			SecurityContextRepository securityContextRepository,
			@Value("${server.servlet.session.cookie.secure:false}") boolean cookieSeguro,
			@Value("${server.servlet.session.cookie.same-site:lax}") String cookieSameSite,
			@Value("${server.servlet.session.cookie.partitioned:false}") boolean cookieParticionado,
			CorsConfigurationSource corsConfigurationSource) throws Exception {
		String sameSite = sameSiteHttp(cookieSameSite);
		CookieCsrfTokenRepository csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
		csrf.setCookiePath("/");
		csrf.setCookieCustomizer(cookie -> cookie
				.path("/")
				.httpOnly(false)
				.secure(cookieSeguro)
				.sameSite(sameSite)
				.partitioned(cookieParticionado));
		CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
		csrfHandler.setCsrfRequestAttributeName(null);

		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource))
				.csrf(config -> config
						.csrfTokenRepository(csrf)
						.csrfTokenRequestHandler(csrfHandler))
				.securityContext(contexto -> contexto.securityContextRepository(securityContextRepository))
				.sessionManagement(sessao -> {
					sessao.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
							.sessionFixation(fixacao -> fixacao.changeSessionId())
							.maximumSessions(1);
				})
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(jsonAuth)
						.accessDeniedHandler(jsonAuth))
				.headers(headers -> headers
						.contentTypeOptions(org.springframework.security.config.Customizer.withDefaults())
						.frameOptions(frame -> frame.deny())
						.referrerPolicy(politica -> politica
								.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
						.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'")))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(logout -> logout
						.logoutUrl("/api/auth/logout")
						.logoutSuccessHandler((request, response, authentication) -> response
								.setStatus(HttpServletResponse.SC_NO_CONTENT))
						.invalidateHttpSession(true)
						.addLogoutHandler((request, response, authentication) -> {
							expirarCookie(response, "MENU_SESSION", true, cookieSeguro, sameSite, cookieParticionado);
							expirarCookie(response, "XSRF-TOKEN", false, cookieSeguro, sameSite, cookieParticionado);
						}))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/auth/csrf", "/actuator/health", "/actuator/health/**")
						.permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout",
								"/api/auth/recuperar-senha", "/api/auth/redefinir-senha").permitAll()
						.requestMatchers(HttpMethod.GET, "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**").hasAnyRole("ADMINISTRADOR", "GESTOR", "ANALISTA")
						.requestMatchers("/api/categorias", "/api/categorias/**").hasAnyRole("ADMINISTRADOR", "GESTOR")
						.requestMatchers(HttpMethod.GET, "/api/insumos", "/api/insumos/**").hasAnyRole("ADMINISTRADOR", "GESTOR", "ANALISTA")
						.requestMatchers("/api/insumos", "/api/insumos/**").hasAnyRole("ADMINISTRADOR", "GESTOR")
						.requestMatchers(HttpMethod.GET, "/api/produtos", "/api/produtos/**").hasAnyRole("ADMINISTRADOR", "GESTOR", "ANALISTA")
						.requestMatchers("/api/produtos", "/api/produtos/**").hasAnyRole("ADMINISTRADOR", "GESTOR")
						.requestMatchers(HttpMethod.GET, "/api/estabelecimentos", "/api/estabelecimentos/**")
						.hasAnyRole("ADMINISTRADOR", "GESTOR", "ANALISTA")
						.requestMatchers("/api/estabelecimentos", "/api/estabelecimentos/**").hasAnyRole("ADMINISTRADOR", "GESTOR")
						.requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("ADMINISTRADOR")
						.requestMatchers(HttpMethod.GET, "/api/pedidos", "/api/pedidos/**")
						.hasAnyRole("ADMINISTRADOR", "GESTOR", "OPERADOR", "ANALISTA")
						.requestMatchers("/api/pedidos", "/api/pedidos/**").hasAnyRole("ADMINISTRADOR", "GESTOR", "OPERADOR")
						.requestMatchers("/api/importacoes", "/api/importacoes/**").hasAnyRole("ADMINISTRADOR", "GESTOR", "OPERADOR")
						.requestMatchers(HttpMethod.GET, "/api/analises/comparar").hasAnyRole("ADMINISTRADOR", "GESTOR", "ANALISTA")
						.requestMatchers(HttpMethod.POST, "/api/analises/simular").hasAnyRole("ADMINISTRADOR", "GESTOR")
						.requestMatchers(HttpMethod.GET, "/api/analises", "/api/analises/**").hasAnyRole("ADMINISTRADOR", "GESTOR", "ANALISTA")
						.requestMatchers("/api/analises", "/api/analises/**").hasAnyRole("ADMINISTRADOR", "GESTOR")
						.requestMatchers(HttpMethod.GET, "/api/config-analise").hasAnyRole("ADMINISTRADOR", "GESTOR", "ANALISTA")
						.requestMatchers("/api/config-analise").hasAnyRole("ADMINISTRADOR", "GESTOR")
						.requestMatchers(HttpMethod.GET, "/api/alertas", "/api/alertas/**").hasAnyRole("ADMINISTRADOR", "GESTOR", "ANALISTA")
						.requestMatchers(HttpMethod.GET, "/api/dashboard").hasAnyRole("ADMINISTRADOR", "GESTOR", "ANALISTA", "OPERADOR")
						.requestMatchers("/api/relatorios/clientes", "/api/relatorios/unidades").hasAnyRole("ADMINISTRADOR", "GESTOR")
						.requestMatchers("/api/relatorios/**").hasAnyRole("ADMINISTRADOR", "GESTOR", "ANALISTA")
						.requestMatchers("/api/unidades", "/api/unidades/**").hasAnyRole("ADMINISTRADOR", "GESTOR")
						.requestMatchers("/api/auditoria", "/api/auditoria/**").hasRole("ADMINISTRADOR")
						.anyRequest().authenticated());

		return http.build();
	}

	private static void expirarCookie(
			jakarta.servlet.http.HttpServletResponse response,
			String nome,
			boolean httpOnly,
			boolean seguro,
			String sameSite,
			boolean particionado) {
		ResponseCookie cookie = ResponseCookie.from(nome, "")
				.path("/")
				.maxAge(0)
				.httpOnly(httpOnly)
				.secure(seguro)
				.sameSite(sameSite)
				.partitioned(particionado)
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private static String sameSiteHttp(String valor) {
		String normalizado = valor == null ? "lax" : valor.trim().toLowerCase(Locale.ROOT);
		return switch (normalizado) {
			case "none" -> "None";
			case "strict" -> "Strict";
			default -> "Lax";
		};
	}
}
