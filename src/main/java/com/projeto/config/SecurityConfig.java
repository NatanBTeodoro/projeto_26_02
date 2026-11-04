package com.projeto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.projeto.repository.UsuarioRepository;
import com.projeto.seguranca.FiltroToken;
import com.projeto.seguranca.JsonAuthHandlers;

@Configuration
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
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
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cors);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JsonAuthHandlers jsonAuth,
			UsuarioRepository usuarios,
			CorsConfigurationSource corsConfigurationSource) throws Exception {
		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource))
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(new FiltroToken(usuarios), AuthorizationFilter.class)
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
				.logout(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
						.requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("ADMINISTRADOR")
						.anyRequest().authenticated());

		return http.build();
	}
}
