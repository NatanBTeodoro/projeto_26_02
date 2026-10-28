package com.projeto.entity;

import java.math.BigDecimal;

import com.projeto.entity.enums.CriterioPopularidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "config_analise")
public class ConfigAnalise {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estabelecimento_id", nullable = false, unique = true)
	private Estabelecimento estabelecimento;

	@Column(name = "suporte_min", nullable = false, precision = 8, scale = 4)
	private BigDecimal suporteMin = new BigDecimal("0.02");

	@Column(name = "confianca_min", nullable = false, precision = 8, scale = 4)
	private BigDecimal confiancaMin = new BigDecimal("0.30");

	@Column(name = "lift_min", nullable = false, precision = 8, scale = 4)
	private BigDecimal liftMin = BigDecimal.ONE;

	@Column(name = "periodo_minimo_dias", nullable = false)
	private Integer periodoMinimoDias = 30;

	@Column(name = "n_alerta_cao", nullable = false)
	private Integer nAlertaCao = 3;

	@Enumerated(EnumType.STRING)
	@Column(name = "criterio_popularidade", nullable = false, length = 20)
	private CriterioPopularidade criterioPopularidade = CriterioPopularidade.PONDERADA;

	@Column(name = "escopo_por_categoria", nullable = false)
	private boolean escopoPorCategoria = true;
}
