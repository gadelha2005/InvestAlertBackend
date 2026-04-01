ALTER TABLE meta
    ADD COLUMN tipo_acompanhamento VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN carteira_id BIGINT NULL;

CREATE TABLE meta_movimentacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meta_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor DECIMAL(19,4) NOT NULL,
    descricao VARCHAR(255),
    data_movimentacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meta_movimentacao_meta FOREIGN KEY (meta_id) REFERENCES meta(id) ON DELETE CASCADE,
    CONSTRAINT fk_meta_movimentacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);
