-- Create carteira_historico table for tracking portfolio evolution
CREATE TABLE IF NOT EXISTS carteira_historico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    carteira_id BIGINT NOT NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(19, 2) NOT NULL,
    tipo_evento VARCHAR(50) NOT NULL,
    descricao VARCHAR(255),
    CONSTRAINT fk_carteira_historico_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_carteira_historico_carteira FOREIGN KEY (carteira_id) REFERENCES carteira(id),
    INDEX idx_carteira_historico_carteira_id (carteira_id),
    INDEX idx_carteira_historico_usuario_id (usuario_id),
    INDEX idx_carteira_historico_data_hora (data_hora),
    INDEX idx_carteira_historico_tipo_evento (tipo_evento)
);
