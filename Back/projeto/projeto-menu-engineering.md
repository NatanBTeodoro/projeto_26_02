# Sistema de Análise de Perfil de Consumidor para Restaurantes

> Documento de projeto derivado da *Especificação da Aplicação v1.1* (28/08/2026 → 09/10/2026), reescrito no formato enxuto de RF / RNF / RN e acrescido de arquitetura (Spring + React), análise crítica, plano de entrega e das especificações técnicas dos materiais didáticos do repositório: **paginação** (`PAGINACAO.md`), **segurança** (`SEGURANCA.md`), **testes** (`exemplo-didatico-testes (1).md`) e **health check / Actuator** (`02 - health-check-actuator.md`).
>
> **Dica rápida:** Requisito Funcional é uma ação (*o sistema faz X*). Regra de Negócio é uma restrição do domínio (*o limite ou condição é Y*). Requisito Não Funcional é uma qualidade mensurável (*o sistema faz X em até Y*).
>
> **Escopo deste documento:** as seções 14 a 17 são **obrigatórias no back-end deste projeto** (não são exemplos de outros sistemas). A seção 18 confronta o repositório atual com essas exigências.

---

## 1. Visão Geral

Ferramenta web de apoio à decisão para bares e restaurantes. A partir do histórico de vendas, o sistema:

1. calcula a **margem de contribuição** real de cada item (preço − custo);
2. mede a **popularidade** de cada item;
3. classifica o cardápio na **matriz de Menu Engineering** (Kasavana & Smith): Estrela, Vaca Leiteira, Enigma, Cão;
4. descobre, por **regras de associação**, quais itens puxam o consumo de outros (cross-sell, ex.: bebidas × pratos);
5. converte os números em **ações recomendadas** (manter, reprecificar, combinar, remover), permite **simular** mudança de preço e emite **alertas** de itens com mau desempenho recorrente.

## 2. Escopo (Principais Entregas)

| Entrega | Descrição |
|---|---|
| Autenticação e perfis | Login, recuperação de senha, 4 perfis (Administrador, Gestor, Operador, Analista) |
| Cadastros | Categorias, produtos, insumos, ficha técnica, histórico de preço/custo |
| Vendas | Registro manual, estorno, importação em lote por CSV |
| Análises | Matriz de Menu Engineering, cross-sell, análise por categoria e curva ABC |
| Decisão | Recomendações, simulador de preço, combos, alertas, comparação entre períodos |
| Governança | Parâmetros configuráveis, log de auditoria, isolamento por estabelecimento |

**Fora do escopo (Trabalhos Futuros):** CRUD completo de clientes, clusterização de perfis, previsão de churn, análise de sentimento, fidelidade, integração direta com PDV via API, controle de estoque, aplicativo mobile.

## 3. Stack e Arquitetura

| Camada | Tecnologia |
|---|---|
| Back-end | Java (LTS) + Spring Boot: Spring Web, Lombok, Spring Security, PostgreSQL Driver (+ dependências da seção 10) |
| Front-end | React (Vite) |
| Banco | PostgreSQL |
| Comunicação | API REST (JSON) |

**Arquitetura proposta: monólito modular com módulo de análise desacoplado.**

```
┌────────────┐   HTTPS    ┌───────────────────────────────────────────┐
│  React SPA │ ─────────► │ Spring Boot (app principal)               │
└────────────┘            │  auth │ cadastro │ vendas │ importação    │
                          │  relatórios │ alertas │ auditoria         │
                          │            ▼ (porta/interface)            │
                          │  ┌─────────────────────────────────────┐  │
                          │  │ módulo analytics-core (sem Web)     │  │
                          │  │ margem · popularidade · matriz ·    │  │
                          │  │ regras de associação · alertas      │  │
                          │  └─────────────────────────────────────┘  │
                          └───────────────────┬───────────────────────┘
                                              ▼
                                        PostgreSQL
```

O módulo `analytics-core` não conhece Spring Web nem as entidades JPA: recebe dados por interfaces e devolve resultados. Isso atende ao espírito do RNF06 e permite, se necessário, extraí-lo depois para um segundo serviço (ver decisão D1 na seção 11).

---

## 4. Requisitos Funcionais (RF)

Categorias: **1** = deve ser totalmente satisfeito · **2** = altamente desejável · **3** = possível, mas eliminável.

### 4.1 Acesso e usuários

| Código | Descrição | Perfil | Cat. | RN |
|---|---|---|---|---|
| RF13 | O usuário deve conseguir autenticar-se com e-mail e senha e receber uma sessão com seu perfil de acesso. | Todos | 1 | RN15, RN23 |
| RF17 | O administrador deve conseguir cadastrar, editar e inativar usuários e atribuir perfis. | Administrador | 1 | RN15 |
| RF32 | O usuário deve conseguir recuperar a senha por e-mail; a sessão deve encerrar por inatividade. | Todos | 2 | — |
| RF31 | O administrador deve conseguir consultar o log de auditoria (usuário, ação, data, valor anterior/novo). | Administrador | 2 | — |
| RF23 | O administrador/gestor deve conseguir gerenciar unidades e comparar o desempenho dos itens entre elas. | Administrador, Gestor | 3 | RN23 |

### 4.2 Cadastros

| Código | Descrição | Perfil | Cat. | RN |
|---|---|---|---|---|
| RF16 | O gestor deve conseguir cadastrar, editar, listar e inativar categorias e vincular produtos a elas. | Gestor | 1 | RN17 |
| RF01 | O gestor deve conseguir cadastrar, editar, listar e inativar produtos com custo de produção e preço de venda. | Gestor | 1 | RN01, RN14 |
| RF18 | O gestor deve conseguir cadastrar insumos e montar a ficha técnica, com cálculo automático do custo do produto. | Gestor | 2 | RN01 |
| RF26 | O gestor deve conseguir marcar produtos como sazonais e definir a janela sazonal. | Gestor | 2 | RN10 |
| RF30 | O sistema deve registrar o histórico de alterações de preço e custo com data de vigência. | Gestor | 1 | RN11 |

### 4.3 Vendas

| Código | Descrição | Perfil | Cat. | RN |
|---|---|---|---|---|
| RF02 | O operador deve conseguir registrar pedidos (itens, quantidade, valor, data/hora, identificador opcional de cliente). | Operador | 1 | RN07, RN11, RN13 |
| RF19 | O operador/gestor deve conseguir estornar uma venda informando o motivo; o registro original é preservado. | Operador, Gestor | 1 | RN07 |
| RF03 | O operador/gestor deve conseguir importar vendas em lote por CSV. | Operador, Gestor | 2 | RN21 |
| RF20 | O sistema deve validar o CSV com pré-visualização, mapeamento de colunas/produtos e relatório de erros por linha antes de confirmar. | Operador, Gestor | 2 | RN21 |

### 4.4 Cálculo e análise (serviço de análise)

| Código | Descrição | Perfil | Cat. | RN |
|---|---|---|---|---|
| RF04 | O sistema deve calcular a margem de contribuição de cada item. | Sistema | 1 | RN03, RN11, RN18 |
| RF05 | O sistema deve calcular o índice de popularidade de cada item no período. | Sistema | 1 | RN02, RN18 |
| RF06 | O sistema deve classificar cada item na matriz (Estrela, Vaca Leiteira, Enigma, Cão). | Sistema | 1 | RN04, RN17 |
| RF07 | O sistema deve minerar regras de associação sobre os pedidos do período. | Sistema | 1 | RN05, RN16, RN22 |
| RF08 | O sistema deve calcular e exibir suporte, confiança e lift de cada regra. | Sistema | 1 | RN05, RN09, RN12 |
| RF22 | O gestor/administrador deve conseguir configurar os parâmetros de análise (suporte, confiança, lift, período mínimo, N do alerta, critério de popularidade, escopo por categoria ou geral). | Gestor, Administrador | 1 | RN04, RN12 |

### 4.5 Relatórios e decisão

| Código | Descrição | Perfil | Cat. | RN |
|---|---|---|---|---|
| RF09 | O gestor/analista deve conseguir gerar a matriz de Menu Engineering por período. | Gestor, Analista | 1 | RN06, RN08 |
| RF10 | O gestor/analista deve conseguir gerar o relatório de cross-sell com as combinações de maior associação. | Gestor, Analista | 1 | RN05, RN09 |
| RF11 | O gestor/analista deve conseguir filtrar as análises por período (dia, mês, trimestre, ano). | Gestor, Analista | 1 | RN06 |
| RF12 | O gestor/analista deve conseguir exportar relatórios em PDF e CSV. | Gestor, Analista | 2 | — |
| RF21 | O sistema deve exibir um dashboard com receita, ticket médio, itens por classificação, alertas e melhores regras. | Gestor, Analista | 2 | RN06 |
| RF14 | O gestor deve conseguir simular uma mudança de preço e ver a nova classificação, sem alterar o cardápio. | Gestor | 2 | RN20 |
| RF15 | O sistema deve alertar quando um item permanecer "Cão" por N períodos consecutivos. | Sistema → Gestor | 2 | RN19 |
| RF24 | O sistema deve gerar recomendação textual de ação para cada item conforme a classificação. | Sistema → Gestor | 2 | RN04 |
| RF25 | O gestor deve conseguir ver sugestões de combos a partir das regras mais relevantes, com margem combinada. | Gestor | 2 | RN05, RN09 |
| RF27 | O gestor/analista deve conseguir comparar dois períodos e ver os itens que mudaram de classe. | Gestor, Analista | 2 | RN06, RN08 |
| RF29 | O gestor/analista deve conseguir ver a análise por categoria (participação no faturamento e na margem) e a curva ABC. | Gestor, Analista | 2 | RN17 |
| RF28 | O gestor deve conseguir ver o histórico de classificações de cada item. | Gestor | 3 | RN08, RN19 |
| RF33 | O gestor deve conseguir ver métricas por cliente identificado (frequência, ticket médio, intervalo entre pedidos). | Gestor | 3 | — |

**Totais:** 16 de categoria 1 · 14 de categoria 2 · 3 de categoria 3 (33 RF).

---

## 5. Requisitos Não Funcionais (RNF)

> **Obs:** no desenvolvimento, os RNF devem ser **testados e evidenciados**. A coluna da direita define como.

| Código | Classe | Descrição | Como testar e evidenciar |
|---|---|---|---|
| RNF01 | Eficiência | Mineração sobre até 100 mil pedidos em até 2 min, de forma assíncrona, sem bloquear a navegação. | Teste de integração com base sintética de 100 mil pedidos; 3 execuções cronometradas (Micrometer/log); print da UI navegável durante o job. |
| RNF02 | Usabilidade | Matriz e cross-sell apresentados visualmente; com ≥ 5 gestores, ≥ 80% identificam a classificação de um item sem ajuda. | Roteiro de teste de usabilidade, formulário e planilha de resultados. |
| RNF03 | Operacional | Funcionar nas duas últimas versões estáveis de Chrome, Firefox e Edge. | Playwright nos três navegadores + prints/relatório. |
| RNF04 | Proteção | Toda requisição a dados exige sessão e perfil autorizado; acessos negados retornam erro e são registrados. | Testes MockMvc com a matriz *perfil × endpoint* (401/403); verificação do registro de negados; OWASP ZAP baseline. |
| RNF05 | Capacidade | Suportar 500 mil pedidos mantendo os tempos do RNF12 em consultas mensais. | Carga sintética de 500 mil pedidos; k6/JMeter com p95; `EXPLAIN` das consultas e índices. |
| RNF06 | Desenvolvimento | Módulo de análise desacoplado da aplicação principal. | Módulo Maven próprio sem dependência de Web/JPA; regra ArchUnit que impede a dependência; diagrama de componentes. |
| RNF07 | Confiança | Reexecutar sobre os mesmos dados e parâmetros dá resultado idêntico; parâmetros ficam registrados com o resultado. | Teste automatizado: duas execuções, comparação campo a campo; checagem do registro de parâmetros. |
| RNF08 | Proteção | Senhas com hash + salt (bcrypt/Argon2); todo tráfego em HTTPS. | Teste unitário (hash ≠ texto, prefixo do algoritmo); `testssl.sh`/`sslscan`; redirecionamento HTTP→HTTPS demonstrado. |
| RNF09 | Legal (LGPD) | Identificador do cliente opcional, pseudonimizado, nunca exibido em agregados. | Teste: coluna armazenada ≠ valor original; busca em logs; revisão das telas T20 e relatórios. |
| RNF10 | Operacional | Importar CSV UTF-8 (`,` ou `;`); exportar PDF e CSV. | Arquivos de amostra nos dois separadores; arquivos exportados anexados como evidência. |
| RNF11 | Confiança | Backup diário automático com restauração testada. | Script `pg_dump` agendado + restauração em banco limpo + comparação de contagem de linhas. |
| RNF12 | Eficiência | Cadastro/consulta em até 3 s; relatórios sobre 100 mil pedidos em até 10 s. | k6/JMeter (p95, ambiente e massa de dados declarados); Lighthouse/DevTools. |
| RNF13 | Usabilidade | Interface responsiva (desktop e tablet) com mensagens de erro claras. | Emulação de dispositivos (1366 px e 768 px) + checklist de mensagens de validação. |
| RNF14 | Desenvolvimento | Cálculos cobertos por testes automatizados (≥ 80%) e conferidos contra exemplo manual. | JaCoCo nos pacotes de cálculo; teste com planilha de exemplo calculada à mão. |
| RNF15 | Desenvolvimento | Comunicação entre serviços por API REST (JSON) autenticada. | Contrato OpenAPI (springdoc); requisição sem credencial retorna 401. |
| RNF16 | Confiança | Falha na análise não afeta cadastro e vendas; execuções com falha são registradas e reexecutáveis. | Teste que força exceção no job: cadastro/venda seguem funcionando; job fica `FALHOU` e é reexecutado. |
| RNF17 | Capacidade | Toda listagem de cadastro, vendas, auditoria, alertas e resultados de análise é **paginada** (Spring Data `Pageable`); tamanho máximo limitado; `sort` inválido responde 400; contrato estável via `PageResponseDTO`. | Testes MockMvc da seção 14: página 0, última incompleta, página além do fim (`content` vazio), `size` acima do teto, `sort` inválido = 400. |
| RNF18 | Proteção | Controles de sessão, CSRF, BCrypt, IDOR, força bruta, headers e Actuator fechado conforme a seção 15 (derivado de `SEGURANCA.md`). | Matriz perfil × endpoint (seção 17.4) + testes de CSRF/IDOR/429. |
| RNF19 | Operacional | Actuator com `/actuator/health` (liveness interno, readiness com banco); detalhes só para autorizado; exposure sem `*` em produção. | Testes da seção 16.4: UP/DOWN do indicator; IT do endpoint. |

**Como implementar RNF04, RNF08, RNF14, RNF17–RNF19:** seções [14](#14-paginação-obrigatória-nas-listagens), [15](#15-segurança-da-api), [16](#16-health-check-e-observabilidade) e [17](#17-estratégia-de-testes-um-teste-por-método).

---

## 6. Regras de Negócio (RN)

**Cálculo e classificação**

- **RN01:** Item só entra no Menu Engineering se tiver custo de produção; senão é sinalizado como "dado incompleto".
- **RN02:** Popularidade = (quantidade vendida do item ÷ quantidade total vendida no período) × 100.
- **RN03:** Margem de contribuição = preço de venda − custo de produção.
- **RN04:** Item é *popular* se sua participação ≥ 70% × (1 ÷ nº de itens analisados) (Kasavana & Smith) e tem margem *alta* se acima da margem média do cardápio ponderada pela quantidade vendida. Estrela: popular + margem alta · Vaca Leiteira: popular + margem baixa · Enigma: não popular + margem alta · Cão: não popular + margem baixa. O gestor pode alternar para média simples (RF22).
- **RN08:** Item novo sem histórico mínimo (ex.: 30 dias) não é classificado; aparece como "em observação".
- **RN10:** Item sazonal é comparado à média do cardápio apenas dentro da própria janela sazonal.
- **RN17:** A classificação é feita por categoria (padrão); o gestor pode optar pelo cardápio inteiro. Categoria com menos de 3 itens elegíveis não é classificada isoladamente.
- **RN18:** Itens vendidos a preço zero (cortesias) não entram em popularidade, margem e associação.

**Associação (cross-sell)**

- **RN05:** Regra relevante exige lift > 1 e suporte mínimo atingido.
- **RN09:** Regra com suporte próximo do mínimo é marcada "estatisticamente frágil".
- **RN12:** Suporte, confiança e lift mínimos têm padrão (sugestão: 2% / 30% / lift > 1) e são ajustáveis; os valores usados são registrados junto ao resultado.
- **RN16:** Mineração só roda com quantidade mínima de pedidos no período (sugestão: 200); senão, "base insuficiente".
- **RN22:** Cada pedido é uma cesta de itens distintos; pedido com um único item distinto não gera regra, mas conta no total do suporte.

**Período, histórico e consistência**

- **RN06:** Análises são sempre recalculadas para o período escolhido, sem misturar períodos.
- **RN07:** Vendas canceladas ou estornadas não entram nos cálculos.
- **RN11:** Cada item vendido guarda preço e custo vigentes na data; análises passadas usam esses valores.
- **RN13:** Pedido tem ≥ 1 item; quantidade inteira > 0; preço unitário ≥ 0; data/hora não futura.
- **RN14:** Produto com vendas não é excluído fisicamente: é inativado; inativos não entram em novos pedidos, mas permanecem nos históricos.
- **RN19:** O alerta de "Cão" conta só períodos consecutivos, de mesma granularidade, em que o item foi classificado Cão; reinicia ao mudar de classe; "em observação" não conta.
- **RN20:** A simulação de preço é descartável e assume popularidade constante (sem elasticidade); o aviso é visível.
- **RN21:** Na importação, pedidos com identificador externo já existente são ignorados; linhas inválidas vão para relatório de erros sem barrar as válidas; produtos não cadastrados exigem mapeamento.

**Acesso**

- **RN15:** Administrador (tudo) · Gestor (produtos, custos, preços, parâmetros, análises) · Operador (registra/importa vendas, **sem** custos e margens) · Analista (somente leitura e exportação). Um perfil por usuário por estabelecimento.
- **RN23:** Todo dado pertence a um estabelecimento (e unidade, quando houver); o usuário só acessa o seu; comparativos entre unidades só para Administrador e Gestor da mesma rede.

---

## 7. Perfis de Usuário

| Perfil | Quem é | Principais RF |
|---|---|---|
| Administrador | Proprietário/responsável pela conta | RF13, RF17, RF22, RF23, RF31 |
| Gestor | Decide cardápio e preços | RF01, RF09–RF12, RF13, RF14–RF16, RF18–RF22, RF23, RF24–RF30, RF32, RF33 (também pode RF02, RF03, RF19, RF20) |
| Operador | Caixa/atendimento | RF02, RF03, RF19, RF20 |
| Analista | Consultor (somente leitura) | RF09–RF12, RF21, RF27, RF29 |
| Serviço de análise | Ator de sistema (job assíncrono/agendado) | RF04–RF08, RF15, RF24 |

## 8. Telas

| Tela | Nome | RF |
|---|---|---|
| T01 | Login | RF13 |
| T02 | Recuperação de senha | RF32 |
| T03 | Dashboard inicial | RF21, RF15 |
| T04 | Lista de produtos | RF01, RF16 |
| T05 | Cadastro/edição de produto e ficha técnica | RF01, RF18, RF26, RF30 |
| T06 | Categorias e insumos | RF16, RF18 |
| T07 | Registro de venda | RF02 |
| T08 | Histórico de vendas e estorno | RF19, RF11 |
| T09 | Importação em lote (CSV) | RF03, RF20 |
| T10 | Matriz de Menu Engineering | RF04–RF06, RF09, RF11, RF12, RF27 |
| T11 | Detalhe do item | RF04, RF05, RF24, RF28, RF30 |
| T12 | Cross-sell e combos | RF07, RF08, RF10–RF12, RF25 |
| T13 | Simulador de preço | RF14 |
| T14 | Central de alertas | RF15 |
| T15 | Análise por categoria e curva ABC | RF11, RF12, RF29 |
| T16 | Comparativo entre unidades | RF23, RF27 |
| T17 | Configurações de análise | RF22 |
| T18 | Usuários e perfis | RF13, RF17 |
| T19 | Log de auditoria | RF31 |
| T20 | Métricas por cliente (opcional) | RF33 |

---

## 9. Modelo de Dados Proposto

| Tabela | Campos-chave / observações |
|---|---|
| `estabelecimento` | id, nome, ativo |
| `unidade` | id, estabelecimento_id (só se RF23 entrar) |
| `usuario` | id, estabelecimento_id, e-mail único, senha_hash, perfil, ativo |
| `categoria` | id, estabelecimento_id, nome, ativa |
| `produto` | id, estabelecimento_id, categoria_id, nome, preco_atual, custo_atual, sazonal, janela_ini/fim, ativo |
| `preco_custo_historico` | produto_id, preco, custo, vigencia_ini, vigencia_fim (RF30) |
| `insumo`, `ficha_tecnica_item` | insumo_id, produto_id, quantidade (RF18) |
| `pedido` | id, estabelecimento_id, **external_id (único por estabelecimento)**, data_hora (UTC), status (ATIVO/ESTORNADO), motivo_estorno, cliente_hash |
| `pedido_item` | pedido_id, produto_id, quantidade, **preco_unitario, custo_unitario** (snapshot, RN11) |
| `analise_execucao` | id, estabelecimento_id, periodo_ini/fim, granularidade, parametros (JSON), versao_dados, status, iniciado/concluido, erro |
| `resultado_classificacao` | execucao_id, produto_id, popularidade, margem, classe |
| `resultado_regra` | execucao_id, antecedente, consequente, suporte, confianca, lift, fragil |
| `alerta` | id, produto_id, tipo, periodos, ativo |
| `config_analise` | estabelecimento_id, parâmetros de RF22 |
| `importacao` | id, arquivo, status, linhas_ok, linhas_erro, relatorio_erros |
| `auditoria` | usuario_id, acao, entidade, valor_anterior, valor_novo, data_hora |

**Convenções:** valores monetários em `DECIMAL(12,2)` / `BigDecimal`; datas em UTC (`Instant`) com conversão para `America/Sao_Paulo` na exibição e no corte de "dia"; índices em `(estabelecimento_id, data_hora)` e `(pedido_id, produto_id)`; migrações versionadas com Flyway.

---

## 10. Estrutura do Projeto e Dependências

### 10.1 Back-end (Maven multi-módulo)

```
menu-engineering/
├── analytics-core/          # cálculo puro: margem, popularidade, matriz, Apriori, alertas (sem Spring Web/JPA)
├── app/                     # Spring Boot
│   └── br.ufsc.menu/
│       ├── auth/            # login, sessão, perfis, recuperação de senha
│       ├── cadastro/        # categoria, produto, insumo, ficha técnica, histórico
│       ├── vendas/          # pedido, estorno
│       ├── importacao/      # CSV: validação, mapeamento, relatório de erros
│       ├── analise/         # jobs, adaptadores para analytics-core, resultados persistidos
│       ├── relatorios/      # matriz, cross-sell, ABC, dashboard, exportação
│       ├── alertas/
│       ├── auditoria/
│       ├── health/          # HealthIndicators (banco, jobs de análise)
│       └── config/          # SecurityFilterChain, CSRF SPA, async, Problem Details, paginação
│           dto/PageResponseDTO.java   # contrato estável da paginação (não devolver Page cru)
└── docker-compose.yml       # PostgreSQL + app + front + proxy HTTPS
```

Pacotes de teste espelham o de produção (`src/test/java/...`): `*Test` unitário, `*IT` integração. **Um método público de serviço/domínio/controller exige ao menos um teste** (feliz + erro/limite quando houver regra).

### 10.2 Dependências

Estado em relação ao `pom.xml` (`spring-boot-starter-parent` **4.1.2**, Java 17, **PostgreSQL**). Inventário na [seção 18](#18-inventário-do-repositório-atual--o-que-ainda-falta).

| Situação no `pom.xml` | Dependência | Para quê |
|---|---|---|
| Presente | `spring-boot-starter-webmvc` (+ `*-webmvc-test`) | API REST, MockMvc |
| Presente | `spring-boot-starter-security` (+ `*-security-test`) | RBAC, sessão, CSRF, hash (RNF04, RNF08, seção 15) |
| Presente | `spring-boot-starter-data-jpa` (+ `*-data-jpa-test`) | Persistência e paginação Spring Data (seção 14) |
| Presente | `postgresql` (runtime) | PostgreSQL |
| Presente | Lombok | Boilerplate |
| Presente | `spring-boot-starter-validation` (+ `*-validation-test`) | Bean Validation (RN13, RNF13, política de senha) |
| Presente | `spring-boot-starter-actuator` (+ `*-actuator-test`) | Health check (RNF19, seção 16) |
| Presente | `spring-boot-starter-mail` (+ `*-mail-test`) | Alerta de login e RF32 |
| Presente | `spring-boot-starter-flyway` + `flyway-database-postgresql` | Esquema versionado; nunca `ddl-auto=create` |
| Presente | `micrometer-registry-prometheus` | Métricas (RNF12) |
| Presente | `springdoc-openapi-starter-webmvc-ui` 3.2.0 | Contrato REST (RNF15) |
| Presente | Apache Commons CSV | RF03/RF20 |
| Presente | OpenPDF | RF12 (sem iText AGPL) |
| Presente (test) | Testcontainers PostgreSQL + `spring-boot-testcontainers` | IT de repositório e health |
| Presente (test) | ArchUnit JUnit 5 | RNF06 |
| Presente (plugin) | JaCoCo (`prepare-agent` + `report` em `verify`) | RNF14 |
| Opcional (ainda não) | Spring Session JDBC | Sessão que sobrevive a reinício |
| Evitar | Kafka, RabbitMQ, Redis, microsserviços | Sem necessidade na escala do projeto |

### 10.3 Front-end

| Item | Escolha sugerida |
|---|---|
| Base | React + Vite (TypeScript recomendado) |
| Rotas | React Router (rotas protegidas por perfil) |
| Dados do servidor | TanStack Query (inclui *polling* do status do job, RNF01) |
| Formulários | React Hook Form + Zod |
| UI responsiva | MUI, Ant Design ou Tailwind + shadcn/ui (RNF13) |
| Gráficos | Recharts: `ScatterChart` + `ReferenceLine` desenha os quadrantes da matriz |
| Testes | Vitest + Testing Library; Playwright (RNF03) |

**Regra de ouro:** nenhum cálculo de negócio (classificação, lift, margem) no front-end. React só exibe o que a API devolve.

---

## 11. Análise Crítica

### 11.1 Pontos fortes

- Proposta com diferencial real: cruza **Menu Engineering** com **cross-sell** (a maioria das ferramentas só faz um).
- Regras de negócio maduras: snapshot de preço/custo (RN11), exclusão lógica (RN14), limites estatísticos (RN09, RN16), aviso de limitação da simulação (RN20).
- RNF mensuráveis, classificados por origem e com meta explícita.
- Escopo com categorias 1/2/3 e fora de escopo declarado.

### 11.2 Inconsistências na especificação

| # | Problema | Onde | Sugestão |
|---|---|---|---|
| 1 | **Referência quebrada:** RF02 e RF33 citam a "Seção 7 / 7.1" como evolução futura, mas na v1.1 a Seção 7 é o Glossário e não existe seção de Trabalhos Futuros. | RF02, RF33, 1.2 | Criar a seção ou corrigir as referências. |
| 2 | **Dependência invertida:** RF15 (cat. 2) e RN19 precisam do histórico de classificações, mas RF28 (esse histórico) é cat. 3. | RF15, RF28 | Persistir `resultado_classificacao` desde o início; RF28 vira só a tela. |
| 3 | **Custo × ficha técnica:** RN01 exige custo e RF01 (cat. 1) pede ficha técnica, mas o cálculo por insumos é RF18 (cat. 2). | RF01, RF18 | No MVP o custo é digitado no produto; ficha técnica entra depois. |
| 4 | **RN05 incompleta:** cita lift e suporte; RN12 também define confiança mínima. | RN05, RN12 | Incluir a confiança em RN05. |
| 5 | **RN06 × RF27:** RN06 proíbe misturar períodos, RF27 compara dois. | RN06, RF27 | Declarar a exceção em RN06. |
| 6 | **RNF04 × RF31:** exige registrar acessos negados, mas auditoria é cat. 2. | RNF04, RF31 | Registro mínimo de negados no MVP. |
| 7 | **Reprodutibilidade × dados que mudam:** estornos e importações tardias alteram períodos passados; RN11 fala em "períodos encerrados" sem defini-los; RNF07 exige resultado idêntico. | RN11, RNF07, RF19, RF03 | Definir **fechamento de período** ou registrar `versao_dados` em cada execução. |
| 8 | **"Elegível" indefinido** (RN17) e sem destino para categorias com menos de 3 itens. | RN17 | Definir elegível = ativo, com custo, fora de "em observação", preço > 0, dentro da janela sazonal; categoria pequena cai na análise geral. |
| 9 | **Números frágeis:** RN09 diz "suporte próximo do mínimo" sem critério; com 200 pedidos e suporte de 2%, a regra vale com **4 pedidos**. | RN09, RN12, RN16 | Critério numérico para "frágil" e contagem absoluta mínima (ex.: ≥ 10 pedidos). |
| 10 | **Campo ausente:** RN21 depende de identificador externo, que a entrada do RF02 não prevê. | RN21, RF02 | Coluna `external_id` única por estabelecimento. |
| 11 | **Perfis × RF desencontrados:** a tabela de perfis omite RF19, RF20, RF23 e RF32 no Gestor; RF04 e RF24 têm origem "Serviço de análise" mas não aparecem no ator de sistema. | Seção 3 | Gerar a tabela a partir da coluna "Origem". |
| 12 | **Operador sem custo × snapshot de custo (RN11):** quem grava o custo no item? | RN15, RN11, RF02 | O servidor preenche o custo; o DTO do operador nunca contém custo. |
| 13 | **Período diário** (RF11) torna popularidade, margem ponderada e mineração estatisticamente fracas; RN08/RN16/RN19 não tratam isso. | RF11 | Classificação e mineração a partir de mensal; diário só para receita e ticket. |
| 14 | **"Hash" do identificador:** hash simples de telefone/CPF se quebra por força bruta (espaço pequeno); o glossário o chama de "irreversível". | RNF09 | HMAC com segredo (*pepper*) fora do banco. |
| 15 | **Dispositivo do operador:** RNF03/RNF13 cobrem desktop e tablet; a T07 será usada no caixa. | RNF13, T07 | Confirmar o dispositivo e a navegação por teclado. |

### 11.3 Caminhos mal definidos ou arriscados

1. **Serviço de análise em Python × stack Spring.** RNF06 diz "ex.: serviço em Python separado" (é exemplo, não imposição), mas RNF15 exige REST autenticado entre serviços. Se a análise for outro processo, surge a pergunta: ela recebe 100 mil pedidos por JSON ou lê o banco? Enviar JSON é pesado; ler o banco com usuário somente-leitura é prático, mas é banco compartilhado. Ver decisão D1.
2. **Mineração genérica demais.** Cardápios têm, em geral, algumas dezenas a poucas centenas de itens; pares e trios saem de uma consulta SQL com `COUNT(DISTINCT pedido_id)` ou de um Apriori próprio de ~100 linhas, mais fácil de testar e de justificar na Fundamentação Teórica do que FP-Growth. Bibliotecas como SPMF e Weka são **GPL**; verifique a compatibilidade com o que você pretende publicar. Limite o tamanho do itemset a 3.
3. **Autenticação em SPA.** O RF13 fala em "sessão" e o RF32 em encerramento por inatividade. Guardar JWT em `localStorage` expõe o token a XSS e dificulta revogar usuário inativado (RF17). Para React + Spring no mesmo domínio, **sessão server-side com cookie HttpOnly/Secure/SameSite + CSRF** atende melhor (timeout nativo em `server.servlet.session.timeout`).
4. **Multi-tenancy (RN23) é transversal.** Todo repositório e toda consulta precisam filtrar por `estabelecimento_id`; esquecer em uma consulta é vazamento entre clientes. Implemente desde o início (um filtro central + teste negativo por endpoint), mas sem unidades.
5. **Importação CSV como cat. 2.** É o único caminho realista para ter volume (RN16, RNF01, RNF05) e para demonstrar o sistema. Suba para cat. 1 ou entregue ao menos um importador simples.
6. **Execução assíncrona.** Períodos arbitrários (RN06) não podem ser pré-calculados. Use tabela de jobs (`analise_execucao`), `@Async` com executor próprio e *polling* no React; cache por (estabelecimento, período, parâmetros, `versao_dados`). Não é preciso fila externa.
7. **Simulação (RF14).** Mudar o preço de um item altera também a **média ponderada de margem** do cardápio; recalcular tudo com o preço novo, não só o item.
8. **RF24 "remover" ignora cross-sell.** Um item Cão pode ser forte antecedente de regras (ex.: entrada que puxa bebida). A recomendação deve consultar o cross-sell antes de sugerir remoção; é onde o sistema entrega seu diferencial.
9. **RF25 sem fórmula.** "Margem combinada" é soma das margens? Com desconto do combo? Defina a fórmula.
10. **Alertas (RF15).** Quando rodam? Sugestão: ao fechar cada mês, com o N configurável contando meses classificados como Cão.
11. **Armadilhas de Spring/Lombok.** `@Data` em entidades JPA causa problemas com `equals/hashCode`, `toString` e carga lazy: use `@Getter/@Setter`. DTOs podem ser *records* Java. Dinheiro em `BigDecimal`, com regra de arredondamento definida (RNF07 exige resultado idêntico).
12. **Exportação.** Em CSV, células começando com `=`, `+`, `-` ou `@` permitem *CSV injection*; escape-as. PDF: evite iText por licença.
13. **Backup (RNF11)** é infraestrutura, não aplicação. Entregue script, agendamento e a restauração testada.

### 11.4 Desnecessário ou em excesso

| Item | Motivo | Sugestão |
|---|---|---|
| Segundo serviço + auth entre serviços (RNF06/RNF15) | Dobra deploy, testes e superfície de ataque para um cálculo leve | Módulo desacoplado no mesmo deploy (D1) |
| RF23, T16, unidades em RN23 | Cat. 3 que contamina o modelo | Só `estabelecimento_id`; unidades em Trabalhos Futuros |
| RF33, T20, RNF09, CPF | Cat. 3 que cria obrigação LGPD; CPF é dado desnecessário | Remover; ou manter só telefone/código com HMAC |
| RF04 + RF05 + RF06 + RF08 | Quatro RF cat. 1 para o mesmo cálculo, inflando a contagem | Fundir em 1–2 RF; as fórmulas já estão nas RN |
| RF30 + RN11 + RF31 | Três mecanismos de histórico | Tabela temporal de preço/custo (vigência) + snapshot no item; auditoria só para ações |
| RF26 / RN10 (sazonalidade) | Muda o denominador da popularidade; complexo | Versão 2, ou só marcação sem cálculo diferenciado |
| RF17 + T18 completos no MVP | Pouco valor para a banca | Seed de usuários por perfil + tela simples |
| RF32 | Exige SMTP configurado | Redefinição pelo administrador no MVP |
| Kafka, RabbitMQ, Redis | Escala não justifica | `@Async` + tabela de jobs |

### 11.5 Lacunas: o que adicionar

| # | Recomendação | Por quê |
|---|---|---|
| 1 | **Cadastro inicial** do estabelecimento e do primeiro Administrador (seed ou tela) | Nenhum RF cobre; RF17 pressupõe que já existe um admin |
| 2 | Logout explícito, política de senha e bloqueio por tentativas | Especificado na seção 15; RNF18 |
| 3 | **Lista de pendências de dados** ("dado incompleto", "em observação", "base insuficiente") | RN01, RN08, RN16 geram estados sem tela nem RF |
| 4 | **Fechamento de período** (ou `versao_dados`) | Resolve a inconsistência 7 |
| 5 | **Gerador de dados sintéticos** com associações plantadas (ex.: prato X → bebida Y) e um **conjunto de referência** calculado à mão (exemplo clássico de Kasavana & Smith) | Sem dados reais, é a única forma de validar RNF01, RNF05, RNF14 |
| 6 | **CI** (GitHub Actions) rodando testes + JaCoCo | Evidência contínua de RNF14; seção 17.5 |
| 7 | Contrato de erro com `ProblemDetail`, paginação e Bean Validation | Seções 14 e 17; RNF17 |
| 8 | Observabilidade (Actuator/Micrometer, log de tempo por requisição) | Seção 16; RNF19 |
| 9 | LGPD além do cliente: e-mail dos usuários, retenção, logs sem dado pessoal | O escopo de dados pessoais é maior que o RNF09 |
| 10 | Fuso horário `America/Sao_Paulo` para "dia" e "mês" | Cortes de período corretos |
| 11 | Limite de tamanho/linhas no upload de CSV | Proteção e RNF12 |
| 12 | `docker-compose` com PostgreSQL, app, front e proxy HTTPS (Caddy/Nginx) | Reprodutibilidade e RNF08; evita CORS (mesma origem) |
| 13 | Parágrafo de **limitações do método** (Menu Engineering ignora custos fixos, sazonalidade e efeito-vitrine) | Honestidade acadêmica e Fundamentação Teórica |

---

## 12. Plano de Entrega Sugerido

| Fase | Objetivo | RF / RNF |
|---|---|---|
| 0 | Esqueleto: Spring + JPA + Validation + Flyway + PostgreSQL, Actuator, SecurityFilterChain, `PageResponseDTO`, React, Docker Compose, CI, gerador de dados, JaCoCo | RNF17, RNF18, RNF19 |
| 1 | Acesso e cadastros **paginados**, com testes por método | RF13, RF17 (simples), RF16, RF01 (custo direto), RF30 |
| 2 | Dados de vendas paginados + importação | RF02, RF19, RF03 + RF20 |
| 3 | **Núcleo analítico (MVP defensável)** + testes unitários das RN em `analytics-core` | RF04–RF06, RF09, RF11, RF07, RF08, RF10, RF22, jobs assíncronos, RNF14 |
| 4 | Decisão | RF24 (com consulta ao cross-sell), RF14, RF15, RF21, RF12, RF31 |
| 5 | Refinamentos | RF18, RF25, RF27, RF29, RF26, RF32 |
| Corte | Só se sobrar tempo | RF23, RF28, RF33 |

Telas do MVP: T01, T04, T05, T07, T08, T09, T10, T12, T17.

## 13. Decisões em Aberto

| ID | Decisão | Opções | Recomendação |
|---|---|---|---|
| D1 | Onde roda a análise | (A) módulo desacoplado no monólito · (B) segundo Spring Boot · (C) Python como na v1.1 | **A**, reescrevendo RNF06/RNF15 ("módulo desacoplado, com interface REST interna opcional"); B só se o orientador exigir processo separado |
| D2 | Multi-tenant | só `estabelecimento_id` · com unidades | Só `estabelecimento_id` |
| D3 | Autenticação | sessão + CSRF · JWT | Sessão + CSRF |
| D4 | Identificador do cliente | CPF · telefone com HMAC · remover | Remover do MVP; depois, telefone com HMAC |
| D5 | Fechamento de período | explícito · `versao_dados` por execução | `versao_dados` (menos burocracia) |
| D6 | Algoritmo | Apriori próprio · FP-Growth via biblioteca | Apriori próprio, itemset ≤ 3, validado contra exemplo manual |

*Pergunte ao orientador:* se RNF06/RNF15 podem ser reescritos (D1) e se o importador CSV pode ser cat. 1.

---

## 14. Paginação (obrigatória nas listagens)

Derivado de `PAGINACAO.md`, aplicado a **este** domínio. Listagens sem paginação violam RNF05/RNF12/RNF17: o banco transfere tudo, a JVM monta milhares de objetos e o JSON trava o React.

### 14.1 Endpoints que devem ser paginados

| Recurso | Método | Ordenação padrão (`@PageableDefault`) |
|---|---|---|
| Categorias | `GET /api/categorias` | `nome` |
| Produtos | `GET /api/produtos` | `nome` |
| Insumos | `GET /api/insumos` | `nome` |
| Pedidos / histórico de vendas | `GET /api/pedidos` | `dataHora,desc` |
| Histórico de preço/custo | `GET /api/produtos/{id}/historico-preco` | `vigenciaIni,desc` |
| Usuários (admin) | `GET /api/usuarios` | `email` |
| Auditoria | `GET /api/auditoria` | `dataHora,desc` |
| Alertas | `GET /api/alertas` | `id,desc` |
| Classificações de uma execução | `GET /api/analises/{id}/classificacoes` | `classe` |
| Regras de associação | `GET /api/analises/{id}/regras` | `lift,desc` |
| Importações | `GET /api/importacoes` | `id,desc` |

Não paginar: login/logout/CSRF, `GET` por id, dashboard agregado, health, exportação CSV/PDF (arquivo, não JSON de lista).

Não manter um `GET` "lista todos" paralelo (como o `/livros` sem página do material didático). Só o contrato paginado.

### 14.2 Fluxo por camada

```
GET /api/produtos?page=1&size=20&sort=nome,asc
        │
        ▼
Controller   Spring monta Pageable; @PageableDefault se a URL omitir parâmetros
        │
        ▼
Service      repassa Pageable (já filtrado por estabelecimento_id) e faz page.map(Dto::from)
        │
        ▼
Repository   JpaRepository.findAll(pageable)  →  SELECT … LIMIT  +  SELECT COUNT
```

| Tipo | Papel |
|---|---|
| `Pageable` | Pedido: página (base 0), tamanho, `Sort` |
| `Page<T>` | Resposta interna Spring: itens + metadados |
| `PageRequest.of(0, 10, Sort.by("nome"))` | Montar `Pageable` só em testes |
| `@PageableDefault(size = 20, sort = "id")` | Padrão da URL |

O service **não** monta o `Pageable`. Usa `Page.map()` (preserva metadados); não usar `.stream()` como na lista completa.

Consultas derivadas também paginam: último parâmetro `Pageable`, retorno `Page`:

```java
Page<Produto> findByEstabelecimentoIdAndAtivoTrue(Long estabelecimentoId, Pageable pageable);
```

### 14.3 Contrato JSON: `PageResponseDTO` próprio

Não devolver `Page<T>` cru (o JSON muda com `serialization-mode` e versão do Spring). DTO genérico sob controle do time:

```java
public record PageResponseDTO<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas,
        boolean ultima) {

    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
```

```java
@GetMapping
public ResponseEntity<PageResponseDTO<ProdutoResponseDTO>> listar(
        @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
    return ResponseEntity.ok(PageResponseDTO.from(produtoService.listar(pageable)));
}
```

Sem `spring.data.web.pageable.serialization-mode=via-dto` se o DTO próprio estiver no controller.

Cada item de `conteudo` é o DTO de recurso (`ProdutoResponseDTO`, etc.). Operador: DTO **sem** custo/margem (RN15).

### 14.4 Parâmetros, limites e erros

| URL | Comportamento |
|---|---|
| `page` | Base **0**. Sem parâmetro: 0 |
| `size` | Itens por página. Sem parâmetro: valor do `@PageableDefault` |
| `sort=campo,asc` | Nome do **atributo Java** (`anoPublicacao` no exemplo didático; aqui `dataHora`, `precoAtual`) |
| `sort` repetido | Critério principal + desempate |
| Sem `sort` | Sempre há ordenação padrão — senão o mesmo registro pode aparecer em duas páginas |
| Página além do fim | `conteudo: []`, HTTP 200, `totalElementos` correto |
| `size` enorme | Cortar em `spring.data.web.pageable.max-page-size=100` |
| `sort` de campo inexistente | **400**, não 500. Tratar `PropertyReferenceException` no handler de erros |

```properties
spring.data.web.pageable.max-page-size=100
```

Filtros de negócio (`categoriaId`, período, `ativo`) são query params **além** de `page`/`size`/`sort` e entram no método do repository junto com o `Pageable`.

### 14.5 Testes mínimos de paginação (por recurso listável)

Para cada `listar(Pageable)`:

1. primeira página com `size` conhecido e `totalElementos` conferido;
2. última página incompleta;
3. página além do fim → `conteudo` vazio, 200;
4. `size` acima do teto → tamanho efetivo ≤ 100;
5. `sort` inválido → 400;
6. usuário de outro estabelecimento não vê registros (RN23 + paginação).

---

## 15. Segurança da API

Derivado de `SEGURANCA.md`, adaptado aos **quatro perfis** (RN15) e à decisão D3 (sessão + CSRF, sem JWT no `localStorage`).

### 15.1 Controles obrigatórios

| Controle | Especificação |
|---|---|
| Hash de senha | `BCryptPasswordEncoder` custo **12**. Senha só no DTO de entrada; mapper **não** copia para a entidade. Resposta nunca inclui hash nem senha. Teto 72 caracteres (limite do BCrypt). |
| Política de senha | Bean Validation `@Size(min = 8, max = 72)` **e** regra de letra + número (`PoliticaSenha`). |
| E-mail | `trim` + minúsculas; único por estabelecimento (ou global, se o login for só e-mail). |
| Autenticação | `UsuarioDetailsService` carrega por e-mail + role persistida. `DaoAuthenticationProvider` com `hideUserNotFoundExceptions(true)` (não enumerar e-mail). |
| Sessão | Após login: criar sessão, `request.changeSessionId()` (session fixation), gravar `SecurityContext` no `HttpSessionSecurityContextRepository`. Cookie `MENU_SESSION`, `HttpOnly`, `SameSite=Lax`, timeout 30 min (`server.servlet.session.timeout`). `maximumSessions(1)`. Logout invalida sessão e apaga cookies (sessão, CSRF). |
| CSRF | Ligado. `CookieCsrfTokenRepository.withHttpOnlyFalse()` + header `X-XSRF-TOKEN`. `GET /api/auth/csrf` antes de mutações. POST de login/cadastro/logout **sem** header → 403. |
| 401/403 JSON | Sem redirecionar para form login. `JsonAuthHandlers`. |
| IDOR | `GET /api/{recurso}/{id}` só se o registro for do `estabelecimento_id` da sessão **e** o perfil puder ver. Cruzar id de outro tenant → 403 (ou 404 unificado, mas consistente). Operador não lê custo/margem. |
| Força bruta | 5 falhas seguidas no mesmo e-mail → HTTP **429**, bloqueio 5 min; sucesso zera o contador. Memória local no MVP; documentar o limite (não compartilha entre instâncias). |
| Headers | `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`, CSP `'self'` para script/estilo/imagem/`connect-src`. Erros sem stack trace (`server.error.include-*=never`). |
| `open-in-view` | `spring.jpa.open-in-view=false`. |
| Segredos | Banco, SMTP e pepper HMAC só em variável de ambiente; não commitar senha real. `COOKIE_SECURE=true` atrás de HTTPS. |
| Schema | Flyway; **proibido** `ddl-auto=create` / `create-drop`. |
| Logs | Sem senha, token ou e-mail completo em INFO. |
| Alerta de login | E-mail `@Async` após sucesso; falha SMTP **não** cancela o login (depende de Spring Mail — ver seção 18). |
| Multi-tenant | Filtro central por `estabelecimento_id` em todo repositório. |

### 15.2 Matriz de autorização (esqueleto)

| Rota | Público | Operador | Analista | Gestor | Administrador |
|---|---|---|---|---|---|
| `GET /api/auth/csrf`, `POST /api/auth/login` | sim | — | — | — | — |
| `POST /api/auth/logout` | — | sim | sim | sim | sim |
| Cadastros produto/categoria | — | não | leitura | sim | sim |
| `POST /api/pedidos`, estorno, CSV | — | sim | não | sim | sim |
| Análises, matriz, cross-sell, exportação | — | não | leitura | sim | sim |
| Parâmetros de análise (RF22) | — | não | não | sim | sim |
| Usuários, auditoria | — | não | não | não | sim |
| `GET /actuator/health` (só `status`) | sim | — | — | — | — |
| Demais Actuator | — | não | não | não | role `ACTUATOR_ADMIN` |

Cada célula vira teste MockMvc (seção 17.4). Acessos negados: 401 (anônimo) ou 403 (autenticado sem perfil) **e** registro mínimo (RNF04), mesmo que RF31 seja cat. 2.

### 15.3 Actuator (cruzamento com a seção 16)

Em produção: `management.endpoints.web.exposure.include=health,info,prometheus` (nunca `*`); `show-details=when-authorized`; cadeia `@Order(1)` com `EndpointRequest`. Preferir porta de management `9090` só na rede interna.

---

## 16. Health check e observabilidade

Derivado de `02 - health-check-actuator.md`, reduzido ao que este sistema precisa. Não copiar ViaCEP/hardware: não há API externa essencial no MVP.

### 16.1 Obrigatório

1. Dependência `spring-boot-starter-actuator`.
2. `GET /actuator/health` — anônimo vê só `{"status":"UP"}` em produção.
3. Indicator próprio `AnaliseHealthIndicator` (ou `MenuHealthIndicator`): consulta leve (`SELECT 1` / `exists` de estabelecimento), **sem** `count()` em `pedido`.
4. Probes: `management.endpoint.health.probes.enabled=true`.
   - **Liveness:** só estado interno (não incluir banco).
   - **Readiness:** `db` + indicator de dados. Jobs de análise **não** derrubam o pod se falharem (RNF16): status de job vai para detalhe ou grupo informativo, não para liveness.
5. Cache `management.endpoint.health.cache.time-to-live=10s`.
6. `server.shutdown=graceful` e `spring.lifecycle.timeout-per-shutdown-phase=30s`.
7. Docker Compose: `HEALTHCHECK` da API em `/actuator/health/readiness`; PostgreSQL com `pg_isready`; API `depends_on: condition: service_healthy`.

### 16.2 Perfis

| Perfil | Exposure | Detalhes |
|---|---|---|
| `dev` | pode `health,info` (evitar `*` mesmo em aula se o repo for público) | `always` |
| `prod` | `health,info,prometheus` | `when-authorized` + porta 9090 |

### 16.3 Micrometer (recomendado)

`micrometer-registry-prometheus` + `http_server_requests_seconds` para evidenciar RNF12. Não expor versão de SO, caminhos de disco, contagens de negócio nem stack no JSON de health.

### 16.4 Testes de health

- Unitário do indicator: banco responde → `UP`; repositório lança → `DOWN` (Mockito, sem rede).
- IT: `GET /actuator/health` 200 com `status` presente; `GET /actuator/health/readiness` 200 com app + PostgreSQL (Testcontainers) saudáveis.

---

## 17. Estratégia de testes (um teste por método)

Derivado de `exemplo-didatico-testes (1).md`. Meta: **cada método público** de domínio, serviço, controller, indicator e conversão DTO tem pelo menos um teste. Regras de negócio (if, limites, exceções) têm teste **feliz**, **erro** e **fronteira**.

Rodar: `.\mvnw test`. Classe `*Test` no mesmo pacote em `src/test/java`.

### 17.1 Princípios

- Um teste, uma regra. Nome descreve o comportamento (`naoClassificaItemSemCusto`), não o método (`testRn01`).
- Padrão AAA: Arrange / Act / Assert.
- `@DisplayName` em português no relatório.
- `@BeforeEach` isola estado; testes não dependem da ordem.
- `@ParameterizedTest` + `@ValueSource` / `@NullAndEmptySource` para inválidos (0, negativo, blank).
- `assertThrows` **e** conferir que o estado não mudou depois da falha.
- Dinheiro em `BigDecimal` (sem `double`/delta). Escala e arredondamento fixos (RNF07).
- Evitar teste trivial de getter/setter.
- Modelo rico em `analytics-core`: a classe protege a regra; o teste faz `new` sem Spring. Services Spring só orquestram (buscar, chamar domínio, salvar).
- Não repetir a mesma RN no service e no domínio: o service testa orquestração (nada é persistido quando o domínio lança).

### 17.2 Pirâmide por camada

| Camada | Ferramenta | O que cobre | O que não cobre |
|---|---|---|---|
| Domínio / `analytics-core` | JUnit 5 puro | RN01–RN22, Apriori, alerta Cão, simulação | Banco, HTTP |
| DTO | JUnit | `fromEntity` / `de()` — campo esquecido ou vazamento de senha/custo | — |
| Repositório | `@DataJpaTest` + Testcontainers PostgreSQL | `find…(Pageable)`, unicidade `external_id`, filtro `estabelecimento_id` | Regras de cálculo |
| Serviço | Mockito | Orquestração, transação, não salvar em erro | Regras já testadas no domínio |
| Controller | MockMvc + Security Test | Status, JSON, CSRF, 401/403/429, `PageResponseDTO` | SQL |
| Health | Mockito + IT | UP/DOWN, endpoint | Internet real |
| Arquitetura | ArchUnit | `analytics-core` sem Spring Web/JPA | — |

`@SpringBootTest` completo só para fumaça (`contextLoads`) e 1–2 ITs (health, segurança de um fluxo). Não é o teste de cada RN.

### 17.3 Matriz: método → testes (núcleo)

Cobrir **todos** os métodos públicos à medida que existirem. Abaixo o núcleo cat. 1; cadastros CRUD seguem o mesmo padrão (criar válido, validação, inativar com venda, IDOR, página).

**Domínio (`analytics-core`) — um teste por método público, mais limites**

| Método | Testes mínimos |
|---|---|
| `calcularMargem(preco, custo)` | preço − custo; custo nulo → incompleto (RN01); cortesia preço 0 não entra (RN18) |
| `calcularPopularidade(qtdItem, qtdTotal)` | fórmula RN02; total 0 não divide |
| `classificar(popular, margemAlta)` | Estrela, Vaca, Enigma, Cão (RN04); item novo → "em observação" (RN08) |
| `itemPopular(participacao, nItens)` | limite exatamente 70% × (1/n); abaixo/acima |
| `margemAlta(item, mediaPonderada)` | igual à média (definir se alta ou não); acima/abaixo |
| `minerar(cestas, minSup, minConf, minLift)` | regra com lift > 1; abaixo do mínimo não sai; cesta de 1 item não gera regra mas conta no suporte (RN22); base < 200 → insuficiente (RN16); frágil no limiar RN09 |
| `suporte` / `confianca` / `lift` | exemplo calculado à mão (planilha RNF14) |
| `avaliarAlertaCao(historico, n)` | N consecutivos dispara; quebra de sequência reinicia; "em observação" não conta (RN19) |
| `simularPreco(...)` | popularidade constante; **recalcula média ponderada do cardápio** (não só o item) |
| `recomendacao(item, regras)` | Cão que é antecedente forte **não** sugere remover sem aviso (RF24) |

**Vendas / cadastro**

| Método | Testes mínimos |
|---|---|
| `registrarPedido` | ≥ 1 item, qtd > 0, data não futura (RN13); snapshot de preço/custo (RN11); DTO operador sem custo; inativo não entra (RN14) |
| `estornar` | original preservado; estornado some dos cálculos (RN07) |
| `importarCsv` | UTF-8 `,` e `;`; `external_id` duplicado ignorado; linha inválida no relatório sem barrar as válidas (RN21) |
| `inativarProduto` | não deleta físico; some de novos pedidos |
| `listar(Pageable)` | os 6 casos da seção 14.5 |

**Auth**

| Método | Testes mínimos |
|---|---|
| `cadastrar` | hash ≠ texto, prefixo `$2a$`/`$2b$`; senha fraca 400; e-mail duplicado |
| `login` | sessão + cookie; CSRF ausente 403; 5 falhas 429; e-mail inexistente mesma mensagem que senha errada |
| `logout` | próxima request 401 |
| `buscarPorId` | dono/admin 200; outro usuário/tenant 403 |

### 17.4 Matriz perfil × endpoint

Para cada endpoint autenticado, no mínimo:

- anônimo → 401;
- perfil proibido → 403 + auditoria de negado;
- perfil permitido → 2xx;
- outro `estabelecimento_id` → 403/404, lista vazia na paginação.

Usar `@WithMockUser(roles = "GESTOR")` (ou o equivalente do UserDetails real) no MockMvc.

### 17.5 Cobertura e CI

- JaCoCo nos pacotes de cálculo: **≥ 80%** (RNF14), com teste contra planilha Kasavana & Smith feita à mão.
- GitHub Actions: `mvn verify` + relatório JaCoCo.
- O teste `ProjetoApplicationTests.contextLoads` **não** conta como cobertura de regra.

### 17.6 Exemplo de forma (domínio, sem Spring)

```java
@Test
@DisplayName("Item sem custo é sinalizado como dado incompleto e não entra na matriz")
void itemSemCustoNaoEntraNaMatriz() {
    ItemAnalise item = ItemAnalise.novo("X", new BigDecimal("10.00"), null, 5);

    assertEquals(SituacaoDado.INCOMPLETO, item.situacao());
    assertTrue(classificador.elegiveis(List.of(item)).isEmpty());
}
```

---

## 18. Inventário do repositório atual — o que ainda falta

Conferência após a troca para PostgreSQL (`com.projeto`, Spring Boot **4.1.2**, Java 17). As **dependências Maven pedidas pelos MDs já estão no `pom.xml`**. O que falta é **código** (domínio, API, segurança, paginação, migrações Flyway). Só existem `ProjetoApplication`, `application.properties` (JDBC PostgreSQL) e `ProjetoApplicationTests.contextLoads`.

### 18.1 Dependências: nada essencial ausente

| Pedido nos MDs | No `pom.xml` |
|---|---|
| Validation, Actuator, Mail (+ starters `*-test`) | sim |
| Data JPA, Security, Web MVC (+ testes) | sim |
| Driver **PostgreSQL** (no lugar de MySQL) | sim |
| Flyway + `flyway-database-postgresql` | sim |
| Micrometer Prometheus, springdoc 3.2.0, Commons CSV, OpenPDF | sim |
| Testcontainers PostgreSQL, ArchUnit, plugin JaCoCo | sim |

**Opcional ainda de fora:** Spring Session JDBC (sessão que sobrevive a reinício). Não é bloqueante.

**Paginação** continua sem JAR extra: `Pageable`/`Page` vêm do Data JPA. Falta o código (`PageResponseDTO`, endpoints).

`mysql-connector-j` foi **removido**. Backup (RNF11) passa a ser `pg_dump`, não `mysqldump`.

### 18.2 Código e configuração ainda inexistentes

| Área | O que os MDs exigem e o repo não tem |
|---|---|
| Segurança | `SecurityFilterChain`, CSRF SPA, BCrypt 12, login/logout, `changeSessionId`, 401 JSON, IDOR, 429, headers, `PoliticaSenha` |
| Paginação | `Pageable` em controller/service; `PageResponseDTO` (o `max-page-size=100` já está no `application.properties`) |
| Health | indicators, probes, graceful shutdown (o starter Actuator já está no pom) |
| Testes | um teste por método; só existe `contextLoads` |
| Persistência | entidades, repositórios, scripts em `db/migration` (`open-in-view=false` já está nas properties) |
| Análise | módulo `analytics-core` |
| Front | React (este diretório é só o back-end) |
| Compose | `docker-compose.yml` com imagem `postgres` |

### 18.3 Ordem sugerida para fechar as lacunas

1. Subir PostgreSQL local (ou Compose) e criar o banco `menu_engineering`; senha via `DB_PASSWORD`.
2. Primeira migração Flyway + esqueleto de segurança (seção 15) e `PageResponseDTO` (seção 14).
3. Primeiro agregado (produto ou usuário) **já paginado e testado por método**.
4. `analytics-core` com testes das RN **antes** da API de relatórios.
5. JaCoCo no CI (`mvn verify`) quando os cálculos existirem.

