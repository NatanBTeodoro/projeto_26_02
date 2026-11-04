CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(72) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_usuario_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS produto (
    id BIGSERIAL PRIMARY KEY,
    categoria_id BIGINT NOT NULL REFERENCES categoria (id),
    nome VARCHAR(150) NOT NULL,
    preco_atual NUMERIC(12, 2) NOT NULL,
    custo_atual NUMERIC(12, 2),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_produto_preco CHECK (preco_atual >= 0)
);

CREATE TABLE IF NOT EXISTS pedido (
    id BIGSERIAL PRIMARY KEY,
    data_hora TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    motivo_estorno VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS pedido_item (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedido (id),
    produto_id BIGINT NOT NULL REFERENCES produto (id),
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(12, 2) NOT NULL,
    custo_unitario NUMERIC(12, 2),
    CONSTRAINT ck_pedido_item_qtd CHECK (quantidade > 0),
    CONSTRAINT ck_pedido_item_preco CHECK (preco_unitario >= 0)
);

CREATE INDEX IF NOT EXISTS idx_pedido_data ON pedido (data_hora);
CREATE INDEX IF NOT EXISTS idx_pedido_item_pedido ON pedido_item (pedido_id);
