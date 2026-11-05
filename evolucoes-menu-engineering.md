# Evoluções do Sistema de Análise de Cardápio

> Complemento ao documento `projeto-menu-engineering-simplificado.md`. Cada item abaixo explica o que é, quais regras de negócio entram, como fazer no back-end e no front-end, como testar e quais cuidados tomar. O documento simplificado continua valendo; aqui estão só as adições.

## Visão geral

| # | Evolução | Esforço | Mexe no esquema? | Depende de |
|---|---|---|---|---|
| 1 | Gráfico da matriz | Baixo | Não | DTO da matriz |
| 2 | Recomendação por classe | Baixo | Não | DTO da matriz |
| 3 | Dashboard inicial | Baixo | Não | Fuso horário correto |
| 4 | Gerador de dados de demonstração | Baixo/Médio | Sim (coluna `demo`) | Produtos e pedidos |
| 5 | Paginação e filtros | Baixo/Médio | Não | Nenhuma |
| 6 | Exportar a matriz em CSV | Baixo | Não | `AnaliseService` |
| 7 | Comparativo entre dois períodos | Médio | Não | `AnaliseService` refatorado |
| 8 | Simulador de preço | Médio | Não | `AnaliseService` refatorado |
| 9 | Ficha técnica e insumos | Médio/Alto | Sim (2 tabelas) | Produtos |
| 10 | Auditoria | Médio | Sim (1 tabela) | Paginação (item 5) |
| 11 | Cross-sell (regras de associação) | Médio/Alto | Não | Dados de demonstração (item 4) |
| 12 | Unir o mesmo produto no pedido | Baixo | Não | Nenhuma |
| 13 | Vários produtos e filtro por categoria | Baixo | Não | Item 12 |
| 14 | Legenda das classes na matriz | Baixo | Não | Nenhuma |

## Ordem sugerida de implementação

1. **Itens 12, 13 e 14**, que melhoram o uso de pedidos e da matriz e não dependem do restante.
2. **Refatorar o `AnaliseService`** (pré-requisito de vários itens, veja abaixo).
3. **Item 4**, para ter dados com que testar tudo o que vem depois.
4. **Itens 1, 2 e 3**, que são rápidos e deixam o sistema com aparência de produto. O item 2 reaproveita os textos da legenda (item 14).
5. **Item 5**, porque o item 10 usa a paginação.
6. **Itens 6, 7 e 8**, que reaproveitam o cálculo da matriz.
7. **Item 11**, que depende de dados com combinações plantadas.
8. **Itens 9 e 10**, por último, por mexerem no modelo de dados.

## Pré-requisito: um `AnaliseService` com núcleo puro

Vários itens (1, 2, 6, 7, 8) reaproveitam o cálculo da matriz. Para isso funcionar bem, separe o serviço em duas camadas:

```java
@Service
public class AnaliseService {

    // Camada 1: busca no banco e agrega por produto
    public AnaliseDTO calcular(LocalDate de, LocalDate ate) {
        List<LinhaAgregada> linhas = agregar(de, ate);
        return classificar(linhas);
    }

    // Camada 2: função pura, sem banco, sem estado. É a que se testa à mão.
    public AnaliseDTO classificar(List<LinhaAgregada> linhas) { ... }
}

public record LinhaAgregada(
    Long produtoId, String produto, String categoria,
    int quantidade, BigDecimal receita, BigDecimal custoTotal, boolean semCusto) {}
```

- `LinhaAgregada` é o que sai da consulta: por produto, a quantidade vendida, a soma de `quantidade × preço` e a soma de `quantidade × custo` dos itens de pedidos `ATIVO` do período.
- `classificar` aplica RN01 a RN04 e devolve o DTO. O comparativo (7) chama essa função duas vezes, e o simulador (8) chama com uma linha modificada.

O `AnaliseDTO` passa a trazer também os limites usados na classificação, necessários para o gráfico (item 1):

```java
public record AnaliseDTO(
    ResumoDTO resumo,                 // pedidos, receita, ticket médio
    BigDecimal limitePopularidade,    // 70% × (1 ÷ itens elegíveis) × 100
    BigDecimal margemMedia,           // média ponderada pela quantidade
    List<LinhaMatrizDTO> itens) {}

public record LinhaMatrizDTO(
    Long produtoId, String produto, String categoria, int quantidade,
    BigDecimal popularidade, BigDecimal margem,
    Classe classe, String recomendacao) {}

public enum Classe { ESTRELA, VACA_LEITEIRA, ENIGMA, CAO, DADO_INCOMPLETO }
```

---

## 1. Gráfico da matriz

### O que é

Um gráfico de dispersão (scatter) com **popularidade no eixo X** e **margem no eixo Y**. Duas linhas de corte dividem o plano em quatro quadrantes, que são as quatro classes. É a imagem clássica do método de Kasavana & Smith e a forma mais rápida de o gestor entender a situação do cardápio.

```
Margem
  ▲   Enigma    │   Estrela
  │             │
  ├─────────────┼──────────────  ← margem média
  │             │
  │   Cão       │   Vaca Leiteira
  └─────────────┴───────────────► Popularidade
                ↑ limite de popularidade
```

### Regras de negócio

- O gráfico mostra exatamente os itens da tabela da matriz para o mesmo período.
- Itens `DADO_INCOMPLETO` não aparecem no gráfico (não têm margem), mas a tela avisa quantos ficaram de fora.
- As linhas de corte são `limitePopularidade` e `margemMedia`, vindas do back-end. **O front não recalcula nada** (regra já existente: o front não recalcula classe nem margem).
- Cor por classe, sempre a mesma em todas as telas: Estrela (verde), Vaca Leiteira (azul), Enigma (amarelo), Cão (vermelho). Não use só a cor para indicar a classe: o tooltip e a legenda também trazem o texto.

### Back-end

Nenhum endpoint novo. Basta o `AnaliseDTO` incluir `limitePopularidade` e `margemMedia` (ver pré-requisito).

### Front-end

Biblioteca sugerida: **Recharts** (`npm install recharts`).

```jsx
import {
  ScatterChart, Scatter, XAxis, YAxis, ZAxis, CartesianGrid,
  Tooltip, ReferenceLine, ResponsiveContainer, Cell
} from "recharts";

const CORES = {
  ESTRELA: "#2e7d32", VACA_LEITEIRA: "#1565c0",
  ENIGMA: "#f9a825", CAO: "#c62828",
};

export default function GraficoMatriz({ analise }) {
  const pontos = analise.itens.filter(i => i.classe !== "DADO_INCOMPLETO");
  const fora = analise.itens.length - pontos.length;

  return (
    <>
      <ResponsiveContainer width="100%" height={420}>
        <ScatterChart margin={{ top: 20, right: 20, bottom: 30, left: 10 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis type="number" dataKey="popularidade" name="Popularidade"
                 unit="%" label={{ value: "Popularidade (%)", position: "bottom" }} />
          <YAxis type="number" dataKey="margem" name="Margem" unit=" R$"
                 label={{ value: "Margem (R$)", angle: -90, position: "insideLeft" }} />
          <ZAxis range={[80, 80]} />
          <ReferenceLine x={analise.limitePopularidade} stroke="#888" strokeDasharray="5 5" />
          <ReferenceLine y={analise.margemMedia} stroke="#888" strokeDasharray="5 5" />
          <Tooltip cursor={{ strokeDasharray: "3 3" }}
                   content={<TooltipItem />} />
          <Scatter data={pontos}>
            {pontos.map(p => <Cell key={p.produtoId} fill={CORES[p.classe]} />)}
          </Scatter>
        </ScatterChart>
      </ResponsiveContainer>
      {fora > 0 && <p>{fora} item(ns) sem custo não aparecem no gráfico.</p>}
    </>
  );
}
```

Converta `popularidade` e `margem` para número ao receber o JSON (o `BigDecimal` chega como número ou texto, conforme a configuração do Jackson).

### Testes

- Back-end: o teste da classificação passa a conferir também `limitePopularidade` e `margemMedia` com os números escritos à mão.
- Front-end: nada obrigatório. Se quiser, um teste de que itens `DADO_INCOMPLETO` ficam fora de `pontos`.

### Cuidados

- **Rótulos sobrepostos.** Com muitos itens, os pontos se sobrepõem. Mostre o nome só no tooltip, não ao lado do ponto.
- **Escala.** Se um item tiver margem muito maior que os outros, o gráfico achata o resto. Deixe o eixo se ajustar sozinho e não fixe o domínio.
- **Poucos itens.** Com 1 ou 2 itens elegíveis as linhas de corte coincidem com os pontos. Mostre um aviso ("poucos itens para uma análise significativa").

### Tamanho do trabalho

Um componente React e dois campos novos no DTO. Meio dia de trabalho.

---

## 2. Recomendação por classe

### O que é

Ao lado de cada item da matriz, uma sugestão de ação baseada na classe dele. Transforma o relatório numa ferramenta de decisão: o gestor vê não só onde cada produto está, mas o que o método recomenda fazer.

### Regras de negócio

| Classe | Recomendação |
|---|---|
| Estrela | Manter o padrão de qualidade e dar destaque no cardápio. Evite mexer no preço sem testar. |
| Vaca Leiteira | Vende bem, mas a margem é baixa. Teste um pequeno aumento de preço ou reduza o custo (porção, fornecedor). |
| Enigma | Margem boa, pouca saída. Promova o item: posição de destaque, sugestão do garçom, combo. |
| Cão | Pouca saída e pouca margem. Reformule a receita ou o preço, ou retire do cardápio. |
| Dado incompleto | Cadastre o custo do produto para que ele seja classificado. |

- A recomendação **depende só da classe**. Não é gravada no banco: é derivada na hora.
- É uma sugestão do método, não uma verdade. A tela deve dizer isso ("sugestão baseada na classe").

### Back-end

A recomendação mora no próprio enum, o que mantém o texto num só lugar:

```java
public enum Classe {
    ESTRELA("Manter o padrão e dar destaque no cardápio. Evite mexer no preço sem testar."),
    VACA_LEITEIRA("Vende bem com margem baixa. Teste um pequeno aumento de preço ou reduza o custo."),
    ENIGMA("Margem boa e pouca saída. Promova o item: destaque, sugestão do garçom ou combo."),
    CAO("Pouca saída e pouca margem. Reformule o item ou retire do cardápio."),
    DADO_INCOMPLETO("Cadastre o custo do produto para que ele seja classificado.");

    private final String recomendacao;
    Classe(String r) { this.recomendacao = r; }
    public String getRecomendacao() { return recomendacao; }
}
```

O `AnaliseService` preenche `LinhaMatrizDTO.recomendacao` com `classe.getRecomendacao()`.

### Front-end

Na tabela, uma coluna "Sugestão" (ou uma linha expansível). Cada classe aparece como um selo colorido, na mesma cor do gráfico. Um filtro "mostrar só Cães" ajuda o gestor a agir sobre um grupo por vez.

### Testes

- Cada valor do enum tem recomendação não vazia.
- A linha de uma Estrela no DTO traz o texto de Estrela (teste dentro do teste de classificação).

### Cuidados

- **Não prometa resultado.** "Aumente o preço" pode derrubar a venda. Por isso o item 8 (simulador) complementa este.
- **Variante mais rica (opcional):** recomendações com números, como "aumentar R$ 1,00 elevaria a margem para R$ X". Isso é o simulador, então não duplique a lógica aqui.

### Tamanho do trabalho

Cinco textos num enum e uma coluna na tabela. Duas horas.

---

## 3. Dashboard inicial

### O que é

Uma página inicial (`/dashboard`) com um resumo do negócio no período: quanto vendeu, quantos pedidos, ticket médio, quais itens mais saem e como o cardápio está distribuído entre as classes.

### Conteúdo

| Bloco | Dado |
|---|---|
| Cartões | Faturamento, quantidade de pedidos, ticket médio |
| Gráfico de linha | Faturamento por dia |
| Lista | Top 5 itens por quantidade vendida |
| Rosca ou barras | Quantidade de itens por classe (Estrela, Vaca, Enigma, Cão) |

O período padrão é "últimos 30 dias", com seletor de datas.

### Regras de negócio

- Só entram pedidos `ATIVO` do período (RN06 e RN07).
- Faturamento = soma de `quantidade × preço unitário` dos itens.
- Ticket médio = faturamento ÷ quantidade de pedidos, escala 2, `HALF_UP`. Sem pedidos, ticket médio é zero (evite divisão por zero).
- A contagem por classe vem do `AnaliseService`, para não existir uma segunda definição de classe.
- O faturamento por dia agrupa pela **data local** do pedido (fuso de São Paulo).

### Back-end

```
GET /api/dashboard?de=&ate=
```

```java
public record DashboardDTO(
    BigDecimal faturamento,
    int totalPedidos,
    BigDecimal ticketMedio,
    List<PontoDiaDTO> faturamentoPorDia,   // data + valor
    List<TopItemDTO> topItens,              // produto + quantidade
    Map<Classe, Integer> itensPorClasse) {}
```

A consulta de faturamento por dia pode ser feita em Java, agrupando os itens que já foram lidos:

```java
Map<LocalDate, BigDecimal> porDia = itens.stream().collect(
    Collectors.groupingBy(i -> i.getPedido().getDataHora().toLocalDate(),
        TreeMap::new,
        Collectors.reducing(BigDecimal.ZERO,
            i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())),
            BigDecimal::add)));
```

Dias sem venda não aparecem no resultado. Preencha os buracos com zero antes de enviar, senão o gráfico "pula" dias.

### Front-end

Cartões simples e dois gráficos com Recharts (`LineChart` e `PieChart` ou `BarChart`). Rota protegida, mesma regra de sessão das outras.

### Testes

Entrada em memória com 3 pedidos conhecidos: conferir faturamento, ticket médio e top 5 escritos à mão. Um teste com intervalo sem pedidos: tudo zero, sem exceção.

### Cuidados

- **Fuso horário.** O servidor do Render roda em UTC. Um pedido às 22h no Brasil seria gravado como 01h do dia seguinte, e o gráfico por dia ficaria errado. Defina o fuso nas duas pontas:
  - `ENV TZ=America/Sao_Paulo` no Dockerfile, ou a variável `TZ` no Render;
  - `spring.jpa.properties.hibernate.jdbc.time_zone=America/Sao_Paulo`;
  - `spring.jackson.time-zone=America/Sao_Paulo`.
- **Muitos dados.** Se o período for grande, a agregação em Java lê todos os itens. Para o volume deste trabalho tudo bem. Se crescer, mova a agregação para uma consulta `GROUP BY`.

### Tamanho do trabalho

Um endpoint, um DTO e uma página com quatro blocos. Um dia de trabalho.

---

## 4. Gerador de dados de demonstração

### O que é

Um recurso que cria um cardápio e centenas de pedidos fictícios, **plantados de propósito** para que a matriz, o dashboard, o comparativo e o cross-sell tenham o que mostrar. Sem ele, na hora da apresentação o sistema abre vazio, ou com dados tão uniformes que todos os itens caem na mesma classe.

### Regras de negócio

- Só o Administrador pode gerar e apagar dados de demonstração.
- Pedidos gerados são marcados com `demo = true`, e dá para apagar **só eles**, sem encostar nos pedidos reais.
- O recurso fica desligado por padrão em produção (`app.demo.habilitado=false`) e ligado no ambiente de demonstração.
- Os pedidos gerados respeitam as regras normais: item com preço e custo copiados do produto (RN11), data não futura (RN13), pelo menos um item.
- A geração usa uma **semente fixa** (`Random(42)`), para que o resultado seja sempre o mesmo. Isso permite testar e permite repetir a demonstração.

### O que plantar

**Cardápio demo (cerca de 12 produtos, 4 categorias)**, desenhado para cobrir todas as classes:

| Perfil | Quantos | Peso de venda | Margem |
|---|---|---|---|
| Estrela | 3 | alto | alta |
| Vaca Leiteira | 3 | alto | baixa |
| Enigma | 3 | baixo | alta |
| Cão | 2 | baixo | baixa |
| Sem custo | 1 | qualquer | (vazio, para mostrar "dado incompleto") |

**Padrões nos pedidos:**

- Mais pedidos nos fins de semana (peso 1,5 a 2 vezes maior).
- Horários concentrados no almoço e no jantar.
- **Combos plantados**, para o cross-sell: por exemplo, 40% dos pedidos com uma pizza levam sobremesa, 60% dos pedidos com hambúrguer levam refrigerante.
- Cerca de 3% dos pedidos estornados, com motivo.
- Entre 1 e 5 itens por pedido, quantidade 1 na maioria dos casos.

### Back-end

```
POST   /api/demo/gerar   { "pedidos": 500, "dias": 60 }   (Administrador)
DELETE /api/demo                                           (Administrador)
```

Esquema: uma coluna a mais em `pedido`, mais uma em `produto` para os produtos demo.

```sql
ALTER TABLE pedido  ADD COLUMN IF NOT EXISTS demo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE produto ADD COLUMN IF NOT EXISTS demo BOOLEAN NOT NULL DEFAULT FALSE;
```

Esqueleto do gerador:

```java
@Service
@ConditionalOnProperty(name = "app.demo.habilitado", havingValue = "true")
public class DemoService {

    private record Combo(String gatilho, String acompanhamento, double probabilidade) {}

    @Transactional
    public int gerar(int quantidadePedidos, int dias) {
        Random r = new Random(42);
        List<Produto> produtos = garantirCardapioDemo();
        List<Combo> combos = List.of(
            new Combo("Pizza Calabresa", "Pudim", 0.40),
            new Combo("Hambúrguer Clássico", "Refrigerante", 0.60));

        for (int i = 0; i < quantidadePedidos; i++) {
            LocalDateTime quando = sortearDataHora(r, dias);   // respeita fim de semana e horário
            Pedido p = new Pedido(quando, true);
            Produto base = sortearPorPeso(r, produtos);
            p.adicionar(base, 1 + (r.nextInt(10) == 0 ? 1 : 0));
            for (Combo c : combos)
                if (c.gatilho().equals(base.getNome()) && r.nextDouble() < c.probabilidade())
                    p.adicionar(buscar(produtos, c.acompanhamento()), 1);
            if (r.nextInt(100) < 3) p.estornar("Pedido lançado por engano");
            pedidoRepository.save(p);
        }
        return quantidadePedidos;
    }

    @Transactional
    public void apagar() {
        pedidoRepository.deleteByDemoTrue();   // apaga os itens junto (cascade)
    }
}
```

### Front-end

Uma seção na página `/usuarios` ou numa página de administração: campos "quantidade de pedidos" e "dias", botão "Gerar dados de demonstração" e botão "Apagar dados de demonstração", com confirmação.

### Testes

- Com a semente fixa, `gerar(200, 30)` cria exatamente 200 pedidos, nenhum com data futura.
- Existe ao menos um produto de cada classe na matriz resultante (confere o desenho do cardápio).
- Pelo menos uma combinação plantada tem lift acima de 1 (ligação com o item 11).
- `apagar()` remove só pedidos `demo = true`.

### Cuidados

- **Aleatório puro não serve.** Se os itens forem sorteados com a mesma probabilidade, todos caem perto da mesma popularidade e o lift fica em 1. O gerador precisa de pesos e de combos.
- **Não misture com dados reais** sem a marca `demo`, senão não há como limpar depois.
- **O estorno mexe na matriz:** confira que os pedidos estornados saem da contagem (RN07) também nos dados gerados.
- **Custo do produto sem ficha:** se você implementar o item 9, o produto demo precisa de ficha ou de custo manual.

### Tamanho do trabalho

Uma classe de serviço, dois endpoints, duas colunas e uma tela simples. Um dia de trabalho, sendo o desenho do cardápio e dos combos a parte que mais exige cuidado.

---

## 5. Paginação e filtros

### O que é

Listas de pedidos e produtos passam a vir em páginas, com filtros. Hoje o documento devolve arrays inteiros, o que funciona com poucos registros, mas fica lento e ilegível com os centenas de pedidos que o item 4 gera.

### Regras de negócio

- Tamanho de página padrão 20, máximo 100. Pedido de página maior volta com o máximo, sem erro.
- Pedidos: ordenados por data, do mais recente para o mais antigo.
- Produtos: ordenados por nome.
- Filtros são opcionais e combinam entre si (E lógico).
- Página fora do alcance devolve lista vazia, não erro.

### Endpoints

```
GET /api/pedidos?de=&ate=&status=&pagina=0&tamanho=20
GET /api/produtos?categoriaId=&ativo=&busca=&pagina=0&tamanho=20
```

| Filtro | Endpoint | Efeito |
|---|---|---|
| `de`, `ate` | pedidos | Intervalo de data/hora |
| `status` | pedidos | `ATIVO` ou `ESTORNADO` |
| `categoriaId` | produtos | Produtos da categoria |
| `ativo` | produtos | Só ativos ou só inativos |
| `busca` | produtos | Parte do nome, sem diferenciar maiúscula |

### Back-end

O documento original dispensou `PageResponseDTO`. Com esta evolução ele passa a existir:

```java
public record PaginaDTO<T>(
    List<T> conteudo, int pagina, int tamanho,
    long totalElementos, int totalPaginas) {

    public static <T> PaginaDTO<T> de(Page<T> p) {
        return new PaginaDTO<>(p.getContent(), p.getNumber(), p.getSize(),
                               p.getTotalElements(), p.getTotalPages());
    }
}
```

Para filtros opcionais, prefira **Specifications** a uma `@Query` com `(:x is null or ...)`. No PostgreSQL, parâmetros nulos nessa forma costumam gerar erro de tipo.

```java
public interface ProdutoRepository
        extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {}

public class ProdutoSpecs {
    public static Specification<Produto> categoria(Long id) {
        return (r, q, cb) -> id == null ? null : cb.equal(r.get("categoria").get("id"), id);
    }
    public static Specification<Produto> ativo(Boolean ativo) {
        return (r, q, cb) -> ativo == null ? null : cb.equal(r.get("ativo"), ativo);
    }
    public static Specification<Produto> nomeContem(String busca) {
        return (r, q, cb) -> (busca == null || busca.isBlank()) ? null
            : cb.like(cb.lower(r.get("nome")), "%" + busca.toLowerCase() + "%");
    }
}
```

No service:

```java
public PaginaDTO<ProdutoDTO> listar(Long categoriaId, Boolean ativo, String busca,
                                    int pagina, int tamanho) {
    Pageable pageable = PageRequest.of(pagina, Math.min(tamanho, 100), Sort.by("nome"));
    Specification<Produto> spec = Specification
        .where(ProdutoSpecs.categoria(categoriaId))
        .and(ProdutoSpecs.ativo(ativo))
        .and(ProdutoSpecs.nomeContem(busca));
    return PaginaDTO.de(produtoRepository.findAll(spec, pageable).map(ProdutoDTO::de));
}
```

Quando uma Specification devolve `null`, o Spring a ignora, o que deixa o filtro opcional.

### Front-end

- Componente `Paginacao` reutilizável (anterior, próxima, "página X de Y").
- Filtros na **query string** da URL (`/pedidos?status=ESTORNADO&pagina=2`), para o botão "voltar" do navegador funcionar e a página poder ser compartilhada.
- O dropdown de produtos na tela de novo pedido **não deve ser paginado**: use uma consulta própria só com produtos ativos (`GET /api/produtos?ativo=true&tamanho=100`) ou um endpoint de listagem leve.

### Testes

- Com 25 produtos e tamanho 10: três páginas, a última com 5 itens.
- Filtro de status devolve só pedidos estornados.
- Combinação de filtros (categoria + ativo).
- `tamanho=1000` é limitado a 100.

### Cuidados

- **Ordem estável.** Sem uma ordenação definida, o mesmo registro pode aparecer em duas páginas. Sempre ordene.
- **Pedido tem itens.** Ao listar pedidos, evite o problema N+1: use `@EntityGraph` ou devolva só o resumo (id, data, total, status) na lista, e os itens no detalhe.
- **Endpoints que mudam de forma.** A lista deixa de ser um array e vira um objeto. Atualize o front junto, senão a tela quebra.

### Tamanho do trabalho

Um DTO genérico, Specifications para produtos e pedidos, um componente de paginação. Um dia de trabalho.

---

## 6. Exportar a matriz em CSV

### O que é

Um botão na tela `/matriz` que baixa um arquivo com a mesma tabela que o gestor está vendo, para abrir no Excel ou no Google Planilhas. O gestor costuma levar o resultado para uma reunião, para um sócio ou para a planilha dele.

### Regras de negócio

- O CSV contém **exatamente as linhas e os valores da matriz exibida** para o mesmo período, inclusive itens "dado incompleto" (sem classe calculada).
- A permissão é a mesma da matriz: qualquer usuário autenticado.
- O nome do arquivo traz o período: `matriz-2026-10-01_a_2026-10-31.csv`.

### Back-end

```
GET /api/analise/exportar?de=&ate=
```

O endpoint reaproveita o `AnaliseService` e não recalcula nada à parte:

```java
@GetMapping("/exportar")
public ResponseEntity<byte[]> exportar(@RequestParam LocalDate de,
                                       @RequestParam LocalDate ate) {
    AnaliseDTO analise = analiseService.calcular(de, ate);
    byte[] csv = csvService.gerar(analise);
    String nome = "matriz-" + de + "_a_" + ate + ".csv";

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .body(csv);
}
```

### Formato do arquivo

```
Produto;Categoria;Quantidade;Popularidade (%);Margem (R$);Classe
Pizza Calabresa;Pizzas;120;18,5000;22,40;Estrela
Suco de Uva;Bebidas;15;2,3000;9,10;Enigma
```

Três detalhes que costumam dar problema:

- **Separador `;` e vírgula decimal.** O Excel em português espera isso. Com `,` como separador e `.` nos decimais, tudo cai numa coluna só.
- **BOM UTF-8** (`EF BB BF`) no início do arquivo. Sem ele, o Excel abre "Calabresa" com os acentos quebrados.
- **Escape de campos.** Se um nome de produto tiver `;`, aspas ou quebra de linha, o campo vai entre aspas e as aspas internas são duplicadas.

```java
@Service
public class CsvService {

    public byte[] gerar(AnaliseDTO analise) {
        StringBuilder sb = new StringBuilder();
        sb.append("Produto;Categoria;Quantidade;Popularidade (%);Margem (R$);Classe\n");
        for (LinhaMatrizDTO l : analise.itens()) {
            sb.append(campo(l.produto())).append(';')
              .append(campo(l.categoria())).append(';')
              .append(l.quantidade()).append(';')
              .append(decimal(l.popularidade())).append(';')
              .append(decimal(l.margem())).append(';')
              .append(l.classe().name()).append('\n');
        }
        byte[] corpo = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] saida = new byte[bom.length + corpo.length];
        System.arraycopy(bom, 0, saida, 0, bom.length);
        System.arraycopy(corpo, 0, saida, bom.length, corpo.length);
        return saida;
    }

    private String campo(String s) {
        if (s == null) return "";
        if (s.contains(";") || s.contains("\"") || s.contains("\n"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private String decimal(BigDecimal v) {
        return v == null ? "" : v.toPlainString().replace('.', ',');
    }
}
```

### Front-end

O `fetch` normal espera JSON, mas aqui é um arquivo. O botão baixa o corpo como blob:

```js
async function exportarCsv(de, ate) {
  const resp = await fetch(`${API_URL}/api/analise/exportar?de=${de}&ate=${ate}`, {
    credentials: "include",
  });
  if (!resp.ok) { /* mostrar a mensagem de erro */ return; }

  const blob = await resp.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `matriz-${de}_a_${ate}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}
```

Não use um `<a href>` direto para o endpoint. Com front e API em domínios diferentes (como no Render), o cookie de sessão pode não ser enviado e o download falha com 401. Com `fetch` e `credentials: "include"` funciona.

### Testes

- Dado um `AnaliseDTO` em memória, o texto gerado bate com o CSV esperado, escrito à mão (cabeçalho, vírgula decimal, `;`).
- Nome de produto com `;` e aspas sai escapado.
- Os três primeiros bytes são o BOM.
- Sem sessão, 401.

### Cuidados

- **Injeção de fórmula.** Se um nome de produto começar com `=`, `+`, `-` ou `@`, o Excel pode interpretá-lo como fórmula. Prefixe esses campos com um apóstrofo ou espaço. Vale para qualquer CSV gerado a partir de dados digitados por usuários.
- **Não use `.` decimal** achando que é "o padrão": no Brasil o Excel não reconhece.

### Tamanho do trabalho

Uma classe `CsvService`, um endpoint, um botão e quatro testes. A funcionalidade mais barata da lista.

---

## 7. Comparativo entre dois períodos

### O que é

O gestor escolhe dois períodos (por exemplo, setembro e outubro) e o sistema mostra, **item a item**, como a classe mudou. Responde à pergunta mais importante depois de uma decisão: "o que eu fiz funcionou?". Um Enigma que virou Estrela depois de uma promoção é a prova de que a promoção deu certo.

### Regras de negócio

- Cada período é classificado **separadamente**, com a mesma função de classificação usada na matriz.
- Um item é comparado por `produtoId`. Itens que existem só em um dos períodos têm situação própria.
- Situações de cada linha:

| Situação | Quando |
|---|---|
| `MELHOROU` | A classe subiu de nível |
| `PIOROU` | A classe desceu de nível |
| `MUDOU` | Mudou de classe sem subir nem descer (Vaca Leiteira ↔ Enigma) |
| `MANTEVE` | Mesma classe nos dois períodos |
| `NOVO` | Só vendeu no período B |
| `SAIU` | Só vendeu no período A |
| `INCOMPLETO` | Sem custo em um dos períodos |

- Nível de cada classe, para decidir "subiu" ou "desceu": Estrela = 3, Vaca Leiteira = 2, Enigma = 2, Cão = 1.
- Cada período precisa ter data inicial menor ou igual à final. Os períodos **podem** ter durações diferentes. Neste caso a quantidade absoluta não é comparável, e a tela deve avisar. A popularidade (percentual) continua comparável.
- Mostrar variação de quantidade (absoluta e percentual), de popularidade (em pontos percentuais) e de margem.

### Back-end

```
GET /api/analise/comparativo?deA=&ateA=&deB=&ateB=
```

```java
public record ComparativoLinhaDTO(
    Long produtoId, String produto,
    Classe classeA, Classe classeB, Situacao situacao,
    int quantidadeA, int quantidadeB,
    BigDecimal popularidadeA, BigDecimal popularidadeB,
    BigDecimal margemA, BigDecimal margemB) {}

public enum Situacao { MELHOROU, PIOROU, MUDOU, MANTEVE, NOVO, SAIU, INCOMPLETO }
```

O cálculo reaproveita o núcleo puro do `AnaliseService`:

```java
public List<ComparativoLinhaDTO> comparar(LocalDate deA, LocalDate ateA,
                                          LocalDate deB, LocalDate ateB) {
    Map<Long, LinhaMatrizDTO> a = indexar(calcular(deA, ateA));
    Map<Long, LinhaMatrizDTO> b = indexar(calcular(deB, ateB));

    Set<Long> todos = new TreeSet<>(a.keySet());
    todos.addAll(b.keySet());

    return todos.stream()
        .map(id -> montarLinha(a.get(id), b.get(id)))
        .toList();
}

private Situacao situacao(LinhaMatrizDTO a, LinhaMatrizDTO b) {
    if (a == null) return Situacao.NOVO;
    if (b == null) return Situacao.SAIU;
    if (a.classe() == Classe.DADO_INCOMPLETO || b.classe() == Classe.DADO_INCOMPLETO)
        return Situacao.INCOMPLETO;
    if (a.classe() == b.classe()) return Situacao.MANTEVE;
    int diff = nivel(b.classe()) - nivel(a.classe());
    return diff > 0 ? Situacao.MELHOROU : diff < 0 ? Situacao.PIOROU : Situacao.MUDOU;
}
```

### Front-end

- Dois seletores de período, com um atalho "comparar com o período anterior de mesmo tamanho" que preenche o período A automaticamente.
- Tabela com colunas: produto, classe A → classe B, setas coloridas (verde, vermelho, cinza), variação de quantidade e de margem.
- Filtro "mostrar só quem mudou de classe".
- Aviso fixo na tela: "A classe é relativa ao conjunto. Um item pode mudar de classe sem ter mudado, porque os limites dependem dos outros itens."

### Testes

Dois conjuntos de `LinhaAgregada` em memória com resultado conhecido:

- Item que passa de Enigma para Estrela vira `MELHOROU`.
- Vaca Leiteira que passa para Enigma vira `MUDOU`.
- Item presente só no período B vira `NOVO`.
- Item sem custo no período A vira `INCOMPLETO`.
- Períodos com datas invertidas devolvem 400.

### Cuidados

- **Efeito relativo.** Os limites (70% ÷ itens elegíveis e margem média) mudam com o conjunto. Se entrarem itens novos no cardápio, itens antigos podem mudar de classe sem terem mudado de comportamento. Explique isso na tela.
- **Duração diferente.** Comparar um mês com uma semana confunde quantidades. Mostre a duração de cada período.
- **Estacionalidade.** Comparar dezembro com janeiro mistura efeito de estação com efeito de decisão. Não há como resolver no sistema; apenas não prometa que a mudança "foi causada" pela ação do gestor.

### Tamanho do trabalho

Um método de serviço, um DTO, um endpoint e uma página. Um dia de trabalho, desde que o núcleo puro do `AnaliseService` já exista.

---

## 8. Simulador de preço

### O que é

"Se eu subir este item em 10%, como ficam a margem e a classe dele? E o resto do cardápio?" O simulador refaz a classificação com um preço ou custo diferente, **sem gravar nada**, e mostra o antes e o depois.

### Regras de negócio

- A simulação usa os mesmos pedidos reais do período escolhido, como se o item tivesse sido vendido ao novo preço (ou custo) durante todo o período.
- O gestor informa um **novo preço**, um **novo custo**, ou os dois. Ao menos um é obrigatório.
- Campo opcional `variacaoQuantidade` (percentual, padrão 0): permite simular que uma alta de preço reduz as vendas. Por exemplo, `-10` significa 10% menos unidades vendidas.
- Margem simulada do item = novo preço − custo médio ponderado do período (ou o novo custo informado).
- A popularidade de **todos** os itens é recalculada, porque a quantidade total mudou. Os limites também.
- A resposta mostra, para o item: classe e números antes e depois. Para os demais: só os que mudaram de classe por efeito da média.
- Mostra também o **lucro estimado do período** antes e depois (soma de quantidade × margem).
- Nada é gravado. O endpoint é `@Transactional(readOnly = true)`.
- Preço novo maior ou igual a zero. Variação de quantidade maior ou igual a −100.

### Back-end

```
POST /api/analise/simulacao
```

É `POST` por receber um corpo, mas não grava nada. O CSRF continua exigido, como em qualquer `POST`.

```java
public record SimulacaoRequestDTO(
    LocalDate de, LocalDate ate, Long produtoId,
    BigDecimal novoPreco, BigDecimal novoCusto, BigDecimal variacaoQuantidade) {}

public record SimulacaoDTO(
    LinhaMatrizDTO antes, LinhaMatrizDTO depois,
    List<MudancaClasseDTO> outrosQueMudaram,
    BigDecimal lucroAntes, BigDecimal lucroDepois) {}
```

O truque é o núcleo puro: copia a lista de linhas, troca só a linha do produto e chama a mesma `classificar`:

```java
@Transactional(readOnly = true)
public SimulacaoDTO simular(SimulacaoRequestDTO req) {
    List<LinhaAgregada> original = agregar(req.de(), req.ate());
    AnaliseDTO antes = classificar(original);

    List<LinhaAgregada> alterada = original.stream()
        .map(l -> l.produtoId().equals(req.produtoId()) ? aplicar(l, req) : l)
        .toList();
    AnaliseDTO depois = classificar(alterada);

    // item: antes e depois; demais: só quem mudou de classe
    ...
}

private LinhaAgregada aplicar(LinhaAgregada l, SimulacaoRequestDTO req) {
    int qtd = ajustarQuantidade(l.quantidade(), req.variacaoQuantidade());
    BigDecimal preco = req.novoPreco() != null ? req.novoPreco() : l.precoMedio();
    BigDecimal custo = req.novoCusto() != null ? req.novoCusto() : l.custoMedio();
    return new LinhaAgregada(l.produtoId(), l.produto(), l.categoria(), qtd,
        preco.multiply(BigDecimal.valueOf(qtd)),
        custo.multiply(BigDecimal.valueOf(qtd)), l.semCusto());
}
```

### Front-end

- Seleção do produto (pode vir de um botão "simular" ao lado de cada item da matriz).
- Campo de novo preço, ou um controle de "+X%", e um campo opcional "queda esperada nas vendas".
- Resultado em dois cartões lado a lado: **Antes** e **Depois**, com a classe, a margem, a popularidade e o lucro do período.
- Lista curta: "Outros itens que mudariam de classe".
- Aviso: "Estimativa baseada no histórico do período. Não prevê a reação real dos clientes."

### Testes

Exemplo à mão com três itens: calcule a classificação original, aumente o preço de um, e confira margem, classe e lucro simulados. Mais:

- `variacaoQuantidade = -100` zera a quantidade do item (e ele sai da classificação, ou fica sem vendas).
- Sem novo preço nem novo custo, 400.
- Nada é gravado: o preço do produto no banco é o mesmo depois da chamada.

### Cuidados

- **Não é uma previsão.** Preço mais alto costuma reduzir a venda, mas o quanto depende do produto. Por isso `variacaoQuantidade` existe e o aviso fica visível.
- **Custo médio ponderado.** Se o custo mudou ao longo do período, use a média ponderada pela quantidade (a mesma ideia da RN03), nunca o `custo_atual` do produto.
- **Efeito na média.** Aumentar a margem de um item sobe a margem média, e outros itens podem deixar de ser "margem alta". É por isso que a resposta lista os outros que mudaram.

### Tamanho do trabalho

Um método no serviço, dois DTOs, um endpoint e uma tela. Um dia e meio, com o núcleo puro já pronto.

---

## 9. Ficha técnica e insumos

### O que é

A **ficha técnica** é a "receita" de um produto: a lista dos insumos que o compõem, com a quantidade de cada um. **Insumo** é qualquer ingrediente ou material comprado para fazer o prato: farinha, queijo, molho, embalagem.

Hoje o gestor digita o `custo_atual` de cada produto à mão. Com a ficha técnica, o custo passa a ser **calculado**: a soma do custo de cada insumo, na quantidade usada.

### Exemplo

**Pizza Calabresa**

| Insumo | Quantidade na receita | Preço de compra | Custo na receita |
|---|---|---|---|
| Farinha de trigo | 0,250 kg | R$ 6,00/kg | R$ 1,50 |
| Mussarela | 0,200 kg | R$ 40,00/kg | R$ 8,00 |
| Calabresa | 0,100 kg | R$ 30,00/kg | R$ 3,00 |
| Molho de tomate | 0,080 kg | R$ 10,00/kg | R$ 0,80 |
| Caixa de pizza | 1 un | R$ 1,70/un | R$ 1,70 |

Custo do produto: **R$ 15,00**. Se a mussarela subir para R$ 48,00/kg, o custo da pizza vai para R$ 16,60 automaticamente, e a margem e a classe dela mudam junto.

### Por que importa

- O custo deixa de ser um chute, e o Menu Engineering só vale se o custo for confiável.
- Mostra o efeito da inflação dos insumos: um aumento no queijo derruba a margem de todos os produtos que o usam.
- Abre análises novas, como "quais produtos mais dependem deste insumo".

### Modelo de dados

| Tabela | Campos |
|---|---|
| `insumo` | id, nome, unidade (`KG`, `L`, `UN`), custo_unitario, ativo |
| `ficha_tecnica_item` | id, produto_id, insumo_id, quantidade |

```sql
CREATE TABLE IF NOT EXISTS insumo (
    id             BIGSERIAL PRIMARY KEY,
    nome           VARCHAR(120) NOT NULL UNIQUE,
    unidade        VARCHAR(5)   NOT NULL,
    custo_unitario NUMERIC(12,4) NOT NULL CHECK (custo_unitario >= 0),
    ativo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS ficha_tecnica_item (
    id         BIGSERIAL PRIMARY KEY,
    produto_id BIGINT NOT NULL REFERENCES produto(id),
    insumo_id  BIGINT NOT NULL REFERENCES insumo(id),
    quantidade NUMERIC(12,3) NOT NULL CHECK (quantidade > 0),
    UNIQUE (produto_id, insumo_id)
);
```

- `produto.custo_atual` passa a ser **derivado** quando o produto tem ficha: soma de `quantidade × insumo.custo_unitario`. Produto sem ficha continua com custo digitado (assim a RN01 segue valendo).
- `pedido_item.custo_unitario` **não muda**. Ele guarda o custo da hora da venda (RN11). Se o queijo subir hoje, os pedidos de ontem não são reescritos.

### Regras de negócio

- Ao registrar um pedido, o custo gravado no item é o custo calculado da ficha **naquele momento**.
- Alterar o custo de um insumo recalcula o custo dos produtos que o usam, mas só afeta pedidos **futuros**.
- Produto com ficha tem o campo de custo bloqueado para edição manual.
- Insumo usado em alguma ficha não é apagado, só inativado (mesma lógica da RN14). Insumo inativo não entra em ficha nova.
- Um insumo aparece uma vez por ficha.
- Quantidades em `BigDecimal` com 3 casas. Custo calculado com escala 2, `HALF_UP`.

### Back-end

```
GET/POST /api/insumos        PUT /api/insumos/{id}
GET /api/produtos/{id}/ficha
PUT /api/produtos/{id}/ficha          (substitui a lista inteira)
```

O `PUT` da ficha recebe a lista completa de `{insumoId, quantidade}` e substitui a anterior numa transação, o que evita lógica de "adicionar, editar e remover" item por item.

```java
@Transactional
public void salvarFicha(Long produtoId, List<FichaItemDTO> itens) {
    Produto p = produtoRepository.findById(produtoId).orElseThrow(...);
    fichaRepository.deleteByProdutoId(produtoId);
    for (FichaItemDTO i : itens) { /* valida e grava */ }
    p.setCustoAtual(calcularCusto(produtoId));
}

private BigDecimal calcularCusto(Long produtoId) {
    return fichaRepository.findByProdutoId(produtoId).stream()
        .map(f -> f.getQuantidade().multiply(f.getInsumo().getCustoUnitario()))
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);
}
```

Ao alterar o custo de um insumo, recalcule o `custo_atual` dos produtos que o usam:

```java
@Transactional
public void atualizarCustoInsumo(Long insumoId, BigDecimal novoCusto) {
    Insumo i = ...; i.setCustoUnitario(novoCusto);
    for (Long produtoId : fichaRepository.produtosQueUsam(insumoId))
        produtoRepository.atualizarCusto(produtoId, calcularCusto(produtoId));
}
```

### Front-end

- Página `/insumos`: lista (paginada, item 5) e formulário.
- Na página do produto, uma seção "Ficha técnica": tabela editável com insumo, quantidade e custo da linha, e o total calculado em tempo real. O total mostrado no front é só uma prévia: o valor oficial é o do back-end.
- Se o produto tem ficha, o campo de custo aparece desabilitado com a explicação "calculado pela ficha técnica".

### Testes

- Exemplo da pizza acima: custo calculado igual a 15,00.
- Alterar o custo da mussarela recalcula 16,60.
- Pedido antigo continua com o custo gravado.
- Insumo em uso não pode ser apagado, só inativado.
- Quantidade zero ou negativa é recusada.

### Cuidados

- **Unidades.** O problema clássico: comprar em kg e usar em gramas. Para o escopo do trabalho, trave **uma unidade por insumo** (a de compra) e peça a quantidade da receita nessa mesma unidade (`0,250 kg`). Conversão automática fica como trabalho futuro.
- **Rendimento e perdas.** Na vida real, 1 kg de calabresa não rende 1 kg limpo. Ignore nesta versão, ou acrescente um campo opcional de fator de perda.
- **Histórico do custo do insumo.** Sem ele, você só conhece o custo atual. Para comparar períodos com custos diferentes (item 7) com precisão, seria preciso uma tabela de vigência, que é bem mais trabalho. O `custo_unitario` gravado em cada `pedido_item` já cobre o essencial.
- **Dados de demonstração (item 4).** Se este item for implementado, o gerador precisa criar insumos e fichas para os produtos demo, ou deixar o custo manual.

### Tamanho do trabalho

Duas tabelas, CRUD de insumos, edição da ficha, ajuste no `PedidoService` e testes do cálculo. Dois a três dias. É a mais pesada das evoluções, mas é a que mais aproxima o sistema de um restaurante real.

---

## 10. Auditoria

### O que é

Um registro de **quem alterou o quê e quando** nos dados sensíveis: preço e custo de produto, custo de insumo, perfil e situação de usuário, estorno de pedido. Mostra o valor anterior e o novo. É o que permite responder "quem mudou o preço da pizza, e quando?".

### O que registrar

| Entidade | Campos auditados |
|---|---|
| Produto | `preco_atual`, `custo_atual`, `ativo` |
| Insumo (item 9) | `custo_unitario`, `ativo` |
| Usuário | `perfil`, `ativo`, redefinição de senha (sem registrar a senha) |
| Pedido | estorno (com o motivo) |

### Regras de negócio

- Cada alteração gera **um registro por campo alterado**, com valor anterior e valor novo.
- Só grava se o valor **mudou de fato**. Salvar o formulário sem alterar nada não gera registro.
- Registros são **imutáveis**: não há `PUT` nem `DELETE` na auditoria.
- Só o Administrador consulta.
- **Nunca** grava senha, nem em texto nem em hash. Para troca de senha, o registro diz apenas "senha redefinida".
- O usuário registrado é o autenticado no momento (pegue do contexto de segurança, nunca do corpo da requisição).
- O registro é gravado **na mesma transação** da alteração: se a alteração falhar, o registro também não existe.

### Modelo de dados

```sql
CREATE TABLE IF NOT EXISTS auditoria (
    id             BIGSERIAL PRIMARY KEY,
    data_hora      TIMESTAMP NOT NULL,
    usuario_email  VARCHAR(255) NOT NULL,
    entidade       VARCHAR(40)  NOT NULL,
    entidade_id    BIGINT       NOT NULL,
    campo          VARCHAR(60)  NOT NULL,
    valor_anterior VARCHAR(255),
    valor_novo     VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_auditoria_entidade ON auditoria (entidade, entidade_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_data ON auditoria (data_hora);
```

### Back-end

A forma mais simples e mais fácil de explicar é uma chamada **explícita** nos serviços, sem listeners do JPA nem Envers:

```java
@Service
public class AuditoriaService {

    public void registrar(String entidade, Long id, String campo, Object antes, Object depois) {
        if (Objects.equals(antes, depois)) return;           // só se mudou
        Auditoria a = new Auditoria();
        a.setDataHora(LocalDateTime.now());
        a.setUsuarioEmail(usuarioAutenticado());             // do SecurityContext
        a.setEntidade(entidade);
        a.setEntidadeId(id);
        a.setCampo(campo);
        a.setValorAnterior(antes == null ? null : antes.toString());
        a.setValorNovo(depois == null ? null : depois.toString());
        auditoriaRepository.save(a);
    }
}
```

Uso no `ProdutoService`:

```java
@Transactional
public ProdutoDTO atualizar(Long id, ProdutoRequestDTO req) {
    Produto p = buscar(id);
    auditoria.registrar("PRODUTO", id, "preco_atual", p.getPrecoAtual(), req.preco());
    auditoria.registrar("PRODUTO", id, "custo_atual", p.getCustoAtual(), req.custo());
    auditoria.registrar("PRODUTO", id, "ativo", p.isAtivo(), req.ativo());
    p.setPrecoAtual(req.preco());
    ...
}
```

Para `BigDecimal`, compare com `compareTo` e não com `equals`, senão `10.0` e `10.00` parecem diferentes. Ajuste o `registrar` para tratar esse caso.

Consulta:

```
GET /api/auditoria?entidade=&entidadeId=&usuario=&de=&ate=&pagina=0&tamanho=20   (Administrador)
```

Usa a paginação do item 5, e é a razão de esse item vir antes.

### Front-end

Página `/auditoria`, visível só para o Administrador, com filtros (entidade, usuário, período) e uma tabela: data/hora, usuário, entidade, campo, de → para. Opcionalmente, um link "ver histórico" na tela do produto que abre a auditoria já filtrada.

### Testes

- Alterar o preço gera um registro com o valor anterior e o novo.
- Salvar sem mudar nada não gera registro.
- Alterar preço e custo juntos gera dois registros.
- Gestor em `/api/auditoria` recebe 403; sem sessão, 401.
- Troca de senha não grava o hash no registro.
- Se a alteração falhar (exceção), nenhum registro fica no banco.

### Cuidados

- **Esquecer de chamar.** Como é explícito, todo serviço novo precisa lembrar de registrar. Para o escopo do trabalho é aceitável. Uma alternativa, mais automática, é um `@EntityListeners` do JPA, mas ela é mais difícil de entender e de testar.
- **Dados pessoais.** O e-mail do usuário é gravado. Isso é normal em auditoria, mas não coloque dados além do necessário.
- **Crescimento.** A tabela só cresce. Para este projeto não é problema.
- **Ordem na transação.** Registre **antes** de sobrescrever o valor da entidade, senão o "valor anterior" já estará perdido.

### Tamanho do trabalho

Uma tabela, um serviço, chamadas nos serviços existentes, um endpoint e uma tela. Um a dois dias.

---

## 11. Cross-sell (regras de associação)

### O que é

A versão proposta é **só pares de itens** (A → B), calculada na hora a partir dos pedidos, sem tabela nova. Ela segue a mesma ideia do `AnaliseService` da matriz.

Responde à pergunta: "quem pede o item A costuma pedir o item B junto?" O resultado é uma lista de regras como *Pizza Calabresa → Sobremesa*, com três números.

| Métrica | Fórmula | Significado |
|---|---|---|
| Suporte | pedidos com A e B ÷ total de pedidos | Quão frequente é a combinação |
| Confiança | pedidos com A e B ÷ pedidos com A | De quem pede A, quantos levam B |
| Lift | confiança ÷ (pedidos com B ÷ total) | Se a associação é real ou só efeito de B ser popular |

O lift é o número mais importante. Lift acima de 1 significa que A e B aparecem juntos mais do que o acaso explicaria. Perto de 1 é independência. Abaixo de 1, um atrapalha o outro.

### Regras de negócio

- Só entram pedidos `ATIVO` dentro do intervalo `de`/`ate` (RN06 e RN07).
- Cada produto conta **uma vez por pedido**, mesmo que a quantidade seja 5. Com o item 12, o pedido já guarda uma linha só por produto; a quantidade somada não vira um segundo produto na regra.
- Pedido com um único produto entra no total de pedidos, mas não gera par.
- Uma regra só é exibida se tiver um mínimo de pedidos juntos (por exemplo 3, em número absoluto), suporte mínimo e confiança mínima. Padrões sugeridos: suporte 5%, confiança 30%, lift maior que 1. O gestor pode ajustar na tela.
- Ordenação por lift e depois por confiança.
- Arredondamento igual ao resto do sistema: `BigDecimal`, escala 4, `HALF_UP`.

### Algoritmo

Para pares, não precisa de Apriori nem de FP-Growth. Uma contagem em `HashMap` resolve:

```java
public List<RegraDTO> calcular(List<Set<Long>> pedidos, int minJuntos,
                               BigDecimal minSuporte, BigDecimal minConfianca) {
    int total = pedidos.size();
    Map<Long, Integer> unitario = new HashMap<>();
    Map<Par, Integer> pares = new HashMap<>();

    for (Set<Long> itens : pedidos) {
        List<Long> lista = new ArrayList<>(itens);
        for (Long id : lista) unitario.merge(id, 1, Integer::sum);
        for (int i = 0; i < lista.size(); i++)
            for (int j = i + 1; j < lista.size(); j++)
                pares.merge(Par.de(lista.get(i), lista.get(j)), 1, Integer::sum);
    }
    // para cada par, gerar as duas direções (A→B e B→A) e filtrar
}
```

- `Par` é um `record` com `menor` e `maior`, para que (1,2) e (2,1) sejam a mesma chave.
- Cada par gera **duas regras**, porque a confiança é diferente em cada direção.
- O método recebe `List<Set<Long>>`, então o teste não precisa de banco, como no teste da matriz.

A consulta que alimenta o cálculo devolve pares (pedido, produto), e você agrupa por pedido em Java:

```java
@Query("""
  select pi.pedido.id, pi.produto.id from PedidoItem pi
  where pi.pedido.status = 'ATIVO'
    and pi.pedido.dataHora between :de and :ate
""")
List<Object[]> itensDoPeriodo(LocalDateTime de, LocalDateTime ate);
```

### API e tela

```
GET /api/analise/associacoes?de=&ate=&suporteMinimo=&confiancaMinima=&minPedidos=
```

Cada linha do retorno traz: produto antecedente, produto consequente, pedidos juntos, suporte, confiança e lift. A permissão é a mesma da matriz.

No front, uma página `/associacoes` (ou uma aba dentro de `/matriz`) com período, campos de limiar e uma tabela com o lift em destaque.

### Exemplo para o teste, feito à mão

Cinco pedidos:

1. Pizza, Refri
2. Pizza, Refri
3. Pizza, Sobremesa
4. Refri
5. Pizza, Refri, Sobremesa

Contagens: Pizza = 4, Refri = 4, Sobremesa = 2. Pizza+Refri = 3, Pizza+Sobremesa = 2, Refri+Sobremesa = 1.

| Regra | Suporte | Confiança | Lift |
|---|---|---|---|
| Pizza → Refri | 0,60 | 0,75 | 0,9375 |
| Pizza → Sobremesa | 0,40 | 0,50 | 1,25 |
| Sobremesa → Pizza | 0,40 | 1,00 | 1,25 |
| Refri → Sobremesa | 0,20 | 0,25 | 0,625 |

*Pizza → Refri* tem confiança mais alta que *Pizza → Sobremesa*, mas o lift é menor que 1, porque o refri é tão popular que aparece em quase tudo. Esse contraste mostra por que o lift importa.

### Como ligar com a matriz

Isso transforma o cross-sell em decisão, e não só em tabela. Duas sugestões automáticas:

- **Enigma associado a Estrela:** o Enigma tem margem alta e pouca saída, e quem pede a Estrela já tende a pedir o Enigma. Sugestão de combo ou de destaque no cardápio.
- **Cão que só vende junto de uma Estrela:** talvez valha mantê-lo como acompanhamento em vez de removê-lo.

Dá para fazer isso cruzando a lista de regras com a classe de cada produto no `AnaliseService`.

### Testes

- O exemplo de cinco pedidos acima, com os números escritos à mão.
- Pedido com o mesmo produto repetido conta uma vez só.
- Pedido estornado não entra.
- Limiares filtram: com `minJuntos = 3`, só *Pizza → Refri* sobra.
- Entrada vazia devolve lista vazia, sem divisão por zero.

### Cuidados

- **Dados de demonstração:** se o gerador (item 4) sortear itens aleatoriamente, todo lift fica perto de 1 e a tela parece vazia. O gerador precisa **plantar combinações**.
- **Poucos pedidos:** com 20 pedidos, qualquer par parece forte. Por isso o mínimo absoluto de pedidos juntos, e vale mostrar na tela o total de pedidos usado.
- **Desempenho:** o custo cresce com o quadrado dos itens por pedido, que em restaurante é pequeno (3 a 6 itens). Para este trabalho não precisa de cache nem de job assíncrono.
- **Correlação não é causa.** O lift diz que dois itens aparecem juntos, não que um provoca o outro.

### Tamanho do trabalho

Uma classe de serviço, um DTO, um método de repository, um endpoint, um teste com o exemplo acima e uma página no front. Nenhuma tabela nova. Fica de fora, como trabalho futuro, combinações com três ou mais itens.

---

## 12. Unir o mesmo produto no pedido

### O que é

Hoje, em `/pedidos`, cada clique em "Adicionar item" cria uma linha nova, mesmo que o produto já esteja na lista daquele pedido. O back-end grava cada linha como um `pedido_item` separado. O mesmo produto aparece duas vezes no pedido, com quantidades separadas.

A evolução junta essas ocorrências: se o produto já entrou naquele pedido, a nova quantidade **soma** na linha que já existe. Não nasce outro item.

### Regras de negócio

- A chave é o `produtoId` dentro do mesmo pedido. Produtos diferentes continuam em linhas diferentes.
- A quantidade resultante é a soma das quantidades informadas. Duas inclusões de 1 viram quantidade 2; uma de 2 e outra de 3 viram 5.
- Preço e custo continuam copiados do cardápio **uma vez**, na hora de gravar (RN11). Não há dois preços para o mesmo produto no mesmo pedido.
- A regra vale no rascunho da tela e de novo no `PedidoService`, para um cliente que envie o mesmo produto repetido no JSON não furar a regra.
- Produto inativo continua recusado. Quantidade final menor ou igual a zero continua recusada.
- Pedidos já gravados não são reescritos. A junção vale para pedidos novos.

### Back-end

Nenhum endpoint novo. No `PedidoService.criar`, agrupe os itens do corpo por `produtoId` antes de criar as linhas:

```java
Map<Long, Integer> quantidades = new LinkedHashMap<>();
for (PedidoDTO.ItemCreate item : dto.itens()) {
    quantidades.merge(item.produtoId(), item.quantidade(), Integer::sum);
}
```

Cada chave vira um único `PedidoItem`.

### Front-end

Em `incluirItem`, se o `produtoId` já estiver em `itens`, some a quantidade nessa linha em vez de dar `push` em outra. A lista do rascunho mostra uma linha por produto, por exemplo `3 × Pizza Calabresa`.

### Testes

- Corpo com o mesmo produto duas vezes, quantidades 2 e 3: o pedido gravado tem uma linha e quantidade 5.
- Dois produtos diferentes continuam em duas linhas.
- No front, o segundo clique no mesmo produto altera a quantidade da linha existente.

### Cuidados

- **Não some pedidos diferentes.** A junção é só dentro do pedido que está sendo montado.
- **Estorno.** O pedido estornado continua um registro só. Juntar linhas não muda o estorno.
- **Cross-sell (item 11).** A regra de associação já conta o produto uma vez por pedido. Com uma linha só, essa contagem fica natural.

### Tamanho do trabalho

Um agrupamento no serviço, um ajuste na lista do rascunho e um teste. Cerca de duas horas.

---

## 13. Vários produtos e filtro por categoria

### O que é

O pedido precisa aceitar **vários tipos de produto de uma vez**, e a escolha não pode ser uma lista única com o cardápio inteiro. O usuário escolhe a categoria e vê só os produtos dela, inclui os que quiser naquele pedido e só então registra.

Hoje o select lista todo produto ativo, e cada inclusão é um produto por clique. Dá para ir clicando, mas não há filtro de categoria e o mesmo produto vira outra linha (isso o item 12 corrige).

### Regras de negócio

- Um pedido pode ter vários produtos diferentes. Cada produto é uma linha, com a própria quantidade.
- O filtro de categoria só muda o que aparece para escolher. Não grava categoria no pedido.
- A opção inicial do filtro é "Todas", com os produtos ativos. Ao escolher uma categoria, a lista mostra só os ativos dela.
- Categoria inativa não aparece no filtro. Produto inativo não aparece na lista.
- Incluir o mesmo produto de novo segue o item 12: soma a quantidade, não cria outra linha.
- O botão "Registrar pedido" continua gravando o rascunho inteiro numa única chamada `POST /api/pedidos`.

### Back-end

Nenhum endpoint novo. A tela usa os que já existem:

```
GET /api/categorias
GET /api/produtos
POST /api/pedidos
```

O filtro é feito no front com `categoriaId` que o produto já devolve.

### Front-end

Na seção de novo pedido, acima do produto:

- Um select "Categoria", com "Todas" e as categorias ativas.
- O select de produto lista só os produtos ativos da categoria escolhida. Trocar a categoria limpa o produto selecionado se ele não pertencer a ela.
- O rascunho é uma lista de linhas (produto e quantidade) que o usuário monta antes de registrar. Vários tipos entram no mesmo pedido.
- Cada linha do rascunho pode ser removida antes de gravar.

### Testes

- Com categoria "Bebidas" selecionada, um produto de "Pizzas" não aparece na lista.
- "Todas" volta a mostrar os ativos de qualquer categoria.
- Dois produtos de categorias diferentes são enviados no mesmo `POST` e gravados em duas linhas.
- Incluir de novo um produto que já está no rascunho soma a quantidade (item 12).

### Cuidados

- **Não pagine esse select.** A lista é o cardápio ativo para lançar venda, não a listagem administrativa do item 5.
- **Categoria sem produto ativo.** Mostre a lista vazia e a mensagem "Nenhum produto ativo nesta categoria", em vez de deixar o select só com "Selecione".

### Tamanho do trabalho

Só a tela `/pedidos`, reusando categorias e produtos. Menos de meio dia, depois do item 12.

---

## 14. Legenda das classes na matriz

### O que é

A tabela de `/matriz` mostra o nome da classe e não explica o que ele quer dizer. A legenda fica na própria página e diz, em uma frase, o que cada classe significa no método de Kasavana & Smith.

### Regras de negócio

| Classe | Significado na legenda |
|---|---|
| Estrela | Popular e com margem alta. É o item que sustenta o cardápio. |
| Vaca leiteira | Popular, mas com margem baixa. Vende bem e rende pouco. |
| Enigma | Pouco popular e com margem alta. Rende bem e sai pouco. |
| Cão | Pouco popular e com margem baixa. Sai pouco e rende pouco. |
| Dado incompleto | Sem custo cadastrado. Fica de fora da classificação até o custo ser informado. |

- A legenda aparece sempre que a matriz é exibida, também quando o período não tem itens.
- O texto depende só da classe. O front não recalcula popularidade nem margem para escrever a legenda.
- As cores, se forem usadas, são as mesmas do gráfico (item 1): Estrela verde, Vaca leiteira azul, Enigma amarelo, Cão vermelho. Dado incompleto fica neutro.
- Quando o item 2 existir, a recomendação de ação usa esses mesmos significados. A legenda explica a classe; a coluna "Sugestão" diz o que fazer. Os dois textos não podem se contradizer.

### Back-end

Nenhum endpoint novo. A classe já vem em cada item de `GET /api/analise`.

### Front-end

Um bloco "O que cada classe significa" acima ou abaixo da tabela em `Matriz.jsx`, com as cinco linhas da tabela acima. O rótulo visível continua o de `rotuloClasse`. A frase fica numa constante ao lado, para não espalhar o texto pela tela.

### Testes

Nada de back-end. Se houver teste de tela, a página da matriz contém as cinco frases. Sem itens no período, a legenda continua visível.

### Cuidados

- **Não explique a conta na legenda.** O corte de popularidade e a margem média já são regra do servidor. A legenda só diz o que a classe representa.
- **Dado incompleto não é uma classe ruim de venda.** A frase precisa deixar claro que falta o custo, senão o gestor trata como Cão.

### Tamanho do trabalho

Um bloco na página `/matriz`. Cerca de uma hora.

---

## Resumo das mudanças no sistema

### Esquema

| Mudança | Item |
|---|---|
| `pedido.demo` e `produto.demo` (booleanos) | 4 |
| Tabela `insumo` | 9 |
| Tabela `ficha_tecnica_item` | 9 |
| Tabela `auditoria` | 10 |

Os demais itens não alteram o esquema. Todas as mudanças entram no script SQL idempotente (`CREATE TABLE IF NOT EXISTS`, `ADD COLUMN IF NOT EXISTS`), e o Hibernate continua em `validate`.

### Endpoints novos

| Método e caminho | Perfil | Item |
|---|---|---|
| `GET /api/dashboard?de=&ate=` | Autenticado | 3 |
| `POST /api/demo/gerar` e `DELETE /api/demo` | Administrador | 4 |
| `GET /api/analise/exportar?de=&ate=` | Autenticado | 6 |
| `GET /api/analise/comparativo?deA=&ateA=&deB=&ateB=` | Autenticado | 7 |
| `POST /api/analise/simulacao` | Autenticado | 8 |
| `GET/POST /api/insumos`, `PUT /api/insumos/{id}` | Autenticado | 9 |
| `GET/PUT /api/produtos/{id}/ficha` | Autenticado | 9 |
| `GET /api/auditoria` | Administrador | 10 |
| `GET /api/analise/associacoes` | Autenticado | 11 |

Os endpoints de pedidos e produtos mudam de forma (array vira página) por causa do item 5.

### Páginas novas

| Rota | Item |
|---|---|
| `/dashboard` | 3 |
| `/comparativo` | 7 |
| `/simulador` (ou painel dentro de `/matriz`) | 8 |
| `/insumos` | 9 |
| `/auditoria` (Administrador) | 10 |
| `/associacoes` (ou aba em `/matriz`) | 11 |

Os itens 1, 2, 6 e 14 entram dentro de `/matriz`, os itens 12 e 13 dentro de `/pedidos`, e o item 4 dentro da área do Administrador.

### Requisitos funcionais sugeridos

| Código | Descrição | Perfil |
|---|---|---|
| RF20 | Exibir a matriz em gráfico de dispersão com os limites de classificação. | Ambos |
| RF21 | Exibir recomendação de ação para cada classe. | Ambos |
| RF22 | Exibir dashboard com faturamento, ticket médio, top itens e itens por classe. | Ambos |
| RF23 | Gerar e apagar dados de demonstração. | Administrador |
| RF24 | Listar pedidos e produtos com paginação e filtros. | Ambos |
| RF25 | Exportar a matriz do período em CSV. | Ambos |
| RF26 | Comparar a classificação de dois períodos. | Ambos |
| RF27 | Simular alteração de preço ou custo de um produto sem gravar. | Ambos |
| RF28 | Cadastrar insumos e ficha técnica, calculando o custo do produto. | Ambos |
| RF29 | Registrar e consultar auditoria de alterações. | Administrador |
| RF30 | Listar regras de associação entre produtos do período. | Ambos |
| RF31 | Somar a quantidade quando o mesmo produto entra mais de uma vez no mesmo pedido. | Ambos |
| RF32 | Montar um pedido com vários produtos, escolhendo antes a categoria exibida. | Ambos |
| RF33 | Exibir na matriz a legenda do que cada classe significa. | Ambos |

### Cuidados que valem para todos os itens

- **Cookie entre domínios.** Front e API ficam em domínios diferentes no Render. Para sessão e CSRF funcionarem, os cookies precisam de `SameSite=None; Secure`, o CORS precisa de `allowCredentials(true)` com a origem exata do front (sem `*`), e todo `fetch` usa `credentials: "include"`. Vale especialmente para o download do CSV (item 6).
- **Fuso horário.** Defina `America/Sao_Paulo` no servidor (item 3), senão datas e faturamento por dia saem errados.
- **Valores em `BigDecimal`.** Dinheiro com escala 2 e percentuais com escala 4, `HALF_UP`, em todo cálculo novo.
- **O front não recalcula.** Classe, margem, lift, custo e totais vêm sempre do back-end. O front só exibe.
- **Teste do núcleo sem banco.** Todo cálculo novo (comparativo, simulação, associações, custo da ficha) deve ter um teste com entrada em memória e resultado conferido à mão.
