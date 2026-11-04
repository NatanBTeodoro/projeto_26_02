package com.projeto.seguranca;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JsonAuthHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		escrever(response, HttpStatus.UNAUTHORIZED, "Autenticação necessária");
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		escrever(response, HttpStatus.FORBIDDEN, "Acesso negado");
	}

	private void escrever(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
		response.setStatus(status.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"mensagem\":\"" + escapar(mensagem) + "\"}");
	}

	private static String escapar(String valor) {
		return valor.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
