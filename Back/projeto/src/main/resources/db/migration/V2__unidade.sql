CREATE TABLE unidade (
    id BIGSERIAL PRIMARY KEY,
    estabelecimento_id BIGINT NOT NULL REFERENCES estabelecimento (id),
    nome VARCHAR(150) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE pedido
    ADD COLUMN unidade_id BIGINT REFERENCES unidade (id);
