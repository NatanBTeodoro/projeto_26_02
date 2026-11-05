package com.projeto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.DashboardDTO;
import com.projeto.entity.Pedido;
import com.projeto.entity.PedidoItem;
import com.projeto.entity.enums.ClasseMenu;
import com.projeto.entity.enums.StatusPedido;
import com.projeto.repository.PedidoRepository;
import com.projeto.util.exception.RegraDeNegocioException;

@Service
@Transactional(readOnly = true)
public class DashboardService {

	private final PedidoRepository pedidoRepository;

	public DashboardService(PedidoRepository pedidoRepository) {
		this.pedidoRepository = pedidoRepository;
	}

	public DashboardDTO.Response consultar(LocalDate de, LocalDate ate) {
		if (de == null || ate == null) {
			throw new RegraDeNegocioException("Informe a data inicial e a data final");
		}
		if (de.isAfter(ate)) {
			throw new RegraDeNegocioException("A data inicial precisa ser anterior ou igual à final");
		}
		Instant inicio = de.atStartOfDay(AnaliseService.FUSO).toInstant();
		Instant fim = ate.plusDays(1).atStartOfDay(AnaliseService.FUSO).toInstant();
		return montar(de, ate, pedidoRepository.findByDataHoraGreaterThanEqualAndDataHoraLessThan(inicio, fim));
	}

	static DashboardDTO.Response montar(LocalDate de, LocalDate ate, List<Pedido> pedidos) {
		List<ClassificadorCardapio.Linha> linhas = LeituraVendas.linhasAtivas(pedidos);
		long totalPedidos = LeituraVendas.contarPedidosAtivos(pedidos);
		BigDecimal faturamento = ClassificadorCardapio.receita(linhas);
		BigDecimal ticket = totalPedidos == 0
				? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
				: faturamento.divide(BigDecimal.valueOf(totalPedidos), 2, RoundingMode.HALF_UP);

		Map<LocalDate, BigDecimal> porDia = new TreeMap<>();
		for (LocalDate dia = de; !dia.isAfter(ate); dia = dia.plusDays(1)) {
			porDia.put(dia, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
		}
		Map<Long, TopAcumulo> porProduto = new java.util.LinkedHashMap<>();
		for (Pedido pedido : pedidos) {
			if (pedido.getStatus() != StatusPedido.ATIVO || pedido.getDataHora() == null) {
				continue;
			}
			LocalDate dia = pedido.getDataHora().atZone(AnaliseService.FUSO).toLocalDate();
			BigDecimal doDia = porDia.getOrDefault(dia, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
			for (PedidoItem item : pedido.getItens()) {
				BigDecimal valor = item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
				doDia = doDia.add(valor);
				porProduto.computeIfAbsent(
						item.getProduto().getId(),
						id -> new TopAcumulo(id, item.getProduto().getNome()))
						.somar(item.getQuantidade());
			}
			if (porDia.containsKey(dia)) {
				porDia.put(dia, doDia.setScale(2, RoundingMode.HALF_UP));
			}
		}

		List<DashboardDTO.PontoDia> serie = porDia.entrySet().stream()
				.map(entrada -> new DashboardDTO.PontoDia(entrada.getKey(), entrada.getValue()))
				.toList();
		List<DashboardDTO.TopItem> top = porProduto.values().stream()
				.sorted(Comparator.comparingLong(TopAcumulo::quantidade).reversed()
						.thenComparing(TopAcumulo::nome, String.CASE_INSENSITIVE_ORDER))
				.limit(5)
				.map(item -> new DashboardDTO.TopItem(item.produtoId, item.nome, item.quantidade))
				.toList();

		Map<ClasseMenu, Integer> porClasse = new EnumMap<>(ClasseMenu.class);
		for (ClasseMenu classe : ClasseMenu.values()) {
			porClasse.put(classe, 0);
		}
		for (ClassificadorCardapio.Item item : ClassificadorCardapio.resultado(linhas).itens()) {
			porClasse.merge(item.classe(), 1, Integer::sum);
		}

		return new DashboardDTO.Response(faturamento, totalPedidos, ticket, serie, top, porClasse);
	}

	private static final class TopAcumulo {
		private final Long produtoId;
		private final String nome;
		private long quantidade;

		private TopAcumulo(Long produtoId, String nome) {
			this.produtoId = produtoId;
			this.nome = nome;
		}

		private void somar(int quantidade) {
			this.quantidade += quantidade;
		}

		private long quantidade() {
			return quantidade;
		}

		private String nome() {
			return nome;
		}
	}
}
