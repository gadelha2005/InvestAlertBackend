-- ========================
-- USUARIO
-- ========================
CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================
-- ATIVO
-- ========================
CREATE TABLE ativo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticker VARCHAR(10) NOT NULL UNIQUE,
    nome VARCHAR(100),
    tipo VARCHAR(50) NOT NULL,
    mercado VARCHAR(50)
);

-- ========================
-- PRECO_ATIVO
-- ========================
CREATE TABLE preco_ativo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ativo_id BIGINT NOT NULL,
    preco DECIMAL(19,4) NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_preco_ativo FOREIGN KEY (ativo_id) REFERENCES ativo(id)
);

-- ========================
-- CARTEIRA
-- ========================
CREATE TABLE carteira (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    CONSTRAINT fk_carteira_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- ========================
-- CARTEIRA_ATIVO
-- ========================
CREATE TABLE carteira_ativo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    carteira_id BIGINT NOT NULL,
    ativo_id BIGINT NOT NULL,
    quantidade DECIMAL(19,8) NOT NULL,
    preco_medio DECIMAL(19,4) NOT NULL,
    data_compra TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carteira_ativo_carteira FOREIGN KEY (carteira_id) REFERENCES carteira(id),
    CONSTRAINT fk_carteira_ativo_ativo FOREIGN KEY (ativo_id) REFERENCES ativo(id)
);

-- ========================
-- ALERTA
-- ========================
CREATE TABLE alerta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    ativo_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    valor_alvo DECIMAL(19,4) NOT NULL,
    notificar_whatsapp BOOLEAN DEFAULT FALSE,
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alerta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_alerta_ativo FOREIGN KEY (ativo_id) REFERENCES ativo(id)
);

-- ========================
-- NOTIFICACAO
-- ========================
CREATE TABLE notificacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    alerta_id BIGINT NOT NULL,
    mensagem TEXT NOT NULL,
    canal VARCHAR(20) NOT NULL,
    data_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lida BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_notificacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_notificacao_alerta FOREIGN KEY (alerta_id) REFERENCES alerta(id)
);

-- ========================
-- META
-- ========================
CREATE TABLE meta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    valor_objetivo DECIMAL(19,4) NOT NULL,
    valor_atual DECIMAL(19,4) DEFAULT 0,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_limite TIMESTAMP,
    CONSTRAINT fk_meta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);