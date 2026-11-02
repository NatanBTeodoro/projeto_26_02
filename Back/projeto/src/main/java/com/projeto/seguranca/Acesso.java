package com.projeto.seguranca;

import com.projeto.entity.enums.Perfil;
import com.projeto.util.exception.OperacaoNaoPermitidaException;

public final class Acesso {

	private Acesso() {
	}

	public static void exigirEscritaCadastro(UsuarioAtual sessao) {
		if (sessao.perfil() != Perfil.ADMINISTRADOR && sessao.perfil() != Perfil.GESTOR) {
			throw new OperacaoNaoPermitidaException("Perfil sem permissão para alterar cadastros");
		}
	}

	public static void exigirAdministrador(UsuarioAtual sessao) {
		if (sessao.perfil() != Perfil.ADMINISTRADOR) {
			throw new OperacaoNaoPermitidaException("Apenas o administrador pode executar esta ação");
		}
	}

	public static void exigirMesmoEstabelecimento(Long estabelecimentoId, UsuarioAtual sessao) {
		if (estabelecimentoId == null || !estabelecimentoId.equals(sessao.estabelecimentoId())) {
			throw new OperacaoNaoPermitidaException("Registro de outro estabelecimento");
		}
	}

	public static void exigirVendas(UsuarioAtual sessao) {
		if (sessao.perfil() == Perfil.ANALISTA) {
			throw new OperacaoNaoPermitidaException("Analista não registra vendas");
		}
	}

	public static void exigirLeituraAnalise(UsuarioAtual sessao) {
		if (sessao.perfil() == Perfil.OPERADOR) {
			throw new OperacaoNaoPermitidaException("Operador não consulta análises");
		}
	}

	public static void exigirEscritaAnalise(UsuarioAtual sessao) {
		if (sessao.perfil() != Perfil.ADMINISTRADOR && sessao.perfil() != Perfil.GESTOR) {
			throw new OperacaoNaoPermitidaException("Perfil sem permissão para alterar a análise");
		}
	}
}
