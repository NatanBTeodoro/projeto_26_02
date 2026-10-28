package com.projeto.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "resultado_regra")
public class ResultadoRegra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "execucao_id", nullable = false)
	private AnaliseExecucao execucao;

	@Column(nullable = false, length = 500)
	private String antecedente;

	@Column(nullable = false, length = 500)
	private String consequente;

	@Column(nullable = false, precision = 12, scale = 6)
	private BigDecimal suporte;

	@Column(nullable = false, precision = 12, scale = 6)
	private BigDecimal confianca;

	@Column(nullable = false, precision = 12, scale = 6)
	private BigDecimal lift;

	@Column(nullable = false)
	private boolean fragil;
}
