package com.projeto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.AnaliseDTO;
import com.projeto.entity.Pedido;
import com.projeto.repository.PedidoRepository;
import com.projeto.util.exception.RegraDeNegocioException;

@Service
@Transactional(readOnly = true)
public class AnaliseService {

	static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

	private final PedidoRepository pedidoRepository;

	public AnaliseService(PedidoRepository pedidoRepository) {
		this.pedidoRepository = pedidoRepository;
	}

	public AnaliseDTO.Response consultar(LocalDate de, LocalDate ate) {
		if (de == null || ate == null) {
			throw new RegraDeNegocioException("Informe a data inicial e a data final");
		}
		if (de.isAfter(ate)) {
			throw new RegraDeNegocioException("A data inicial precisa ser anterior ou igual à final");
		}
		Instant inicio = de.atStartOfDay(FUSO).toInstant();
		Instant fim = ate.plusDays(1).atStartOfDay(FUSO).toInstant();
		List<Pedido> pedidos = pedidoRepository.findByDataHoraGreaterThanEqualAndDataHoraLessThan(inicio, fim);
		List<ClassificadorCardapio.Linha> linhas = LeituraVendas.linhasAtivas(pedidos);
		long quantidadePedidos = LeituraVendas.contarPedidosAtivos(pedidos);
		BigDecimal receita = ClassificadorCardapio.receita(linhas);
		BigDecimal ticket = quantidadePedidos == 0
				? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
				: receita.divide(BigDecimal.valueOf(quantidadePedidos), 2, RoundingMode.HALF_UP);
		ClassificadorCardapio.Resultado resultado = ClassificadorCardapio.resultado(linhas);
		List<AnaliseDTO.Item> itens = resultado.itens().stream()
				.map(item -> new AnaliseDTO.Item(
						item.produtoId(),
						item.nome(),
						item.categoria(),
						item.quantidade(),
						item.popularidade(),
						item.margem(),
						item.classe(),
						item.recomendacao()))
				.toList();
		return new AnaliseDTO.Response(
				quantidadePedidos,
				receita,
				ticket,
				resultado.limitePopularidade(),
				resultado.margemMedia(),
				itens);
	}
}
