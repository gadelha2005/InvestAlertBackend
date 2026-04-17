ALTER TABLE usuario
    ADD COLUMN reset_token VARCHAR(255),
    ADD COLUMN reset_token_expiracao DATETIME;
