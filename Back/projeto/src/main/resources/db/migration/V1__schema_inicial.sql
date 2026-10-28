CREATE TABLE estabelecimento (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento (id),
    email VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(72) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_usuario_email UNIQUE (email)
);

CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento (id),
    nome VARCHAR(120) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE insumo (
    id BIGSERIAL PRIMARY KEY,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento (id),
    nome VARCHAR(150) NOT NULL,
    unidade_medida VARCHAR(30) NOT NULL,
    custo_unitario NUMERIC(12, 2),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE produto (
    id BIGSERIAL PRIMARY KEY,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento (id),
    categoria_id BIGINT REFERENCES categoria (id),
    nome VARCHAR(150) NOT NULL,
    preco_atual NUMERIC(12, 2) NOT NULL,
    custo_atual NUMERIC(12, 2),
    sazonal BOOLEAN NOT NULL DEFAULT FALSE,
    janela_ini DATE,
    janela_fim DATE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE preco_custo_historico (
    id BIGSERIAL PRIMARY KEY,
    produto_id BIGINT NOT NULL REFERENCES produto (id),
    preco NUMERIC(12, 2) NOT NULL,
    custo NUMERIC(12, 2),
    vigencia_ini TIMESTAMPTZ NOT NULL,
    vigencia_fim TIMESTAMPTZ
);

CREATE TABLE ficha_tecnica_item (
    id BIGSERIAL PRIMARY KEY,
    produto_id BIGINT NOT NULL REFERENCES produto (id),
    insumo_id BIGINT NOT NULL REFERENCES insumo (id),
    quantidade NUMERIC(12, 4) NOT NULL,
    CONSTRAINT uk_ficha_produto_insumo UNIQUE (produto_id, insumo_id)
);

CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento (id),
    external_id VARCHAR(80),
    data_hora TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    motivo_estorno VARCHAR(255),
    cliente_hash VARCHAR(128),
    CONSTRAINT uk_pedido_estab_external UNIQUE (estabelecimento_id, external_id)
);

CREATE TABLE pedido_item (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedido (id),
    produto_id BIGINT NOT NULL REFERENCES produto (id),
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(12, 2) NOT NULL,
    custo_unitario NUMERIC(12, 2),
    CONSTRAINT ck_pedido_item_qtd CHECK (quantidade > 0)
);

CREATE INDEX idx_pedido_estab_data ON pedido (estabelecimento_id, data_hora);
CREATE INDEX idx_pedido_item_pedido_produto ON pedido_item (pedido_id, produto_id);

CREATE TABLE config_analise (
    id BIGSERIAL PRIMARY KEY,
    estabelecimento_id BIGINT NOT NULL UNIQUE REFERENCES estabelecimento (id),
    suporte_min NUMERIC(8, 4) NOT NULL DEFAULT 0.02,
    confianca_min NUMERIC(8, 4) NOT NULL DEFAULT 0.30,
    lift_min NUMERIC(8, 4) NOT NULL DEFAULT 1.00,
    periodo_minimo_dias INTEGER NOT NULL DEFAULT 30,
    n_alerta_cao INTEGER NOT NULL DEFAULT 3,
    criterio_popularidade VARCHAR(20) NOT NULL DEFAULT 'PONDERADA',
    escopo_por_categoria BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE analise_execucao (
    id BIGSERIAL PRIMARY KEY,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento (id),
    periodo_ini TIMESTAMPTZ NOT NULL,
    periodo_fim TIMESTAMPTZ NOT NULL,
    granularidade VARCHAR(20) NOT NULL,
    parametros TEXT,
    versao_dados VARCHAR(64),
    status VARCHAR(20) NOT NULL,
    iniciado_em TIMESTAMPTZ,
    concluido_em TIMESTAMPTZ,
    erro TEXT
);

CREATE TABLE resultado_classificacao (
    id BIGSERIAL PRIMARY KEY,
    execucao_id BIGINT NOT NULL REFERENCES analise_execucao (id),
    produto_id BIGINT NOT NULL REFERENCES produto (id),
    popularidade NUMERIC(12, 6),
    margem NUMERIC(12, 2),
    classe VARCHAR(30) NOT NULL
);

CREATE TABLE resultado_regra (
    id BIGSERIAL PRIMARY KEY,
    execucao_id BIGINT NOT NULL REFERENCES analise_execucao (id),
    antecedente VARCHAR(500) NOT NULL,
    consequente VARCHAR(500) NOT NULL,
    suporte NUMERIC(12, 6) NOT NULL,
    confianca NUMERIC(12, 6) NOT NULL,
    lift NUMERIC(12, 6) NOT NULL,
    fragil BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE alerta (
    id BIGSERIAL PRIMARY KEY,
    produto_id BIGINT NOT NULL REFERENCES produto (id),
    tipo VARCHAR(40) NOT NULL,
    periodos INTEGER NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE importacao (
    id BIGSERIAL PRIMARY KEY,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento (id),
    arquivo VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    linhas_ok INTEGER NOT NULL DEFAULT 0,
    linhas_erro INTEGER NOT NULL DEFAULT 0,
    relatorio_erros TEXT
);

CREATE TABLE auditoria (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT REFERENCES usuario (id),
    acao VARCHAR(80) NOT NULL,
    entidade VARCHAR(80) NOT NULL,
    entidade_id BIGINT,
    valor_anterior TEXT,
    valor_novo TEXT,
    data_hora TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
