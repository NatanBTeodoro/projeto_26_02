package com.projeto.entity;

import java.time.Instant;

import com.projeto.entity.enums.Granularidade;
import com.projeto.entity.enums.StatusAnalise;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "analise_execucao")
public class AnaliseExecucao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estabelecimento_id", nullable = false)
	private Estabelecimento estabelecimento;

	@Column(name = "periodo_ini", nullable = false)
	private Instant periodoIni;

	@Column(name = "periodo_fim", nullable = false)
	private Instant periodoFim;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Granularidade granularidade;

	@Column(columnDefinition = "TEXT")
	private String parametros;

	@Column(name = "versao_dados", length = 64)
	private String versaoDados;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusAnalise status = StatusAnalise.PENDENTE;

	@Column(name = "iniciado_em")
	private Instant iniciadoEm;

	@Column(name = "concluido_em")
	private Instant concluidoEm;

	@Column(columnDefinition = "TEXT")
	private String erro;
}
