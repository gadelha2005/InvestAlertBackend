UPDATE ativo
SET tipo = CASE
    WHEN UPPER(COALESCE(ticker, '')) LIKE '%-USD'
        OR UPPER(COALESCE(mercado, '')) LIKE '%CRYPTO%'
        OR UPPER(COALESCE(nome, '')) LIKE '%BITCOIN%'
        OR UPPER(COALESCE(nome, '')) LIKE '%ETHEREUM%'
        OR UPPER(COALESCE(nome, '')) LIKE '%SOLANA%'
        OR UPPER(COALESCE(nome, '')) LIKE '%CARDANO%'
        OR UPPER(COALESCE(nome, '')) LIKE '%DOGECOIN%'
        OR UPPER(COALESCE(nome, '')) LIKE '%XRP%'
        THEN 'CRIPTOMOEDA'

    WHEN UPPER(COALESCE(ticker, '')) LIKE '^%'
        OR UPPER(COALESCE(nome, '')) LIKE 'INDICE %'
        OR UPPER(COALESCE(nome, '')) LIKE '% IBOV%'
        OR UPPER(COALESCE(nome, '')) LIKE '%S&P%'
        OR UPPER(COALESCE(nome, '')) LIKE '%NASDAQ%'
        OR UPPER(COALESCE(nome, '')) LIKE '%DOW JONES%'
        THEN 'INDICE'

    WHEN UPPER(COALESCE(nome, '')) LIKE '%ETF%'
        OR UPPER(COALESCE(nome, '')) LIKE '%EXCHANGE TRADED FUND%'
        OR UPPER(COALESCE(nome, '')) LIKE '%ISHARES%'
        OR UPPER(COALESCE(nome, '')) LIKE '%INDEX FUND%'
        OR UPPER(COALESCE(nome, '')) LIKE '%INDICE ETF%'
        THEN 'ETF'

    WHEN (
            UPPER(COALESCE(mercado, '')) IN ('B3', 'BVMF', 'BVSP', 'SAO', 'SAOPAULO')
            OR UPPER(COALESCE(ticker, '')) LIKE '%.SA'
        )
        AND (
            UPPER(COALESCE(nome, '')) LIKE '%FII%'
            OR UPPER(COALESCE(nome, '')) LIKE '%FDO IMOB%'
            OR UPPER(COALESCE(nome, '')) LIKE '%FUNDO IMOB%'
            OR UPPER(COALESCE(nome, '')) LIKE '%FUNDO DE INVESTIMENTO IMOBILIARIO%'
            OR UPPER(COALESCE(nome, '')) LIKE '%LOGISTICA%'
            OR UPPER(COALESCE(nome, '')) LIKE '%LAJES%'
            OR UPPER(COALESCE(nome, '')) LIKE '%SHOPPING%'
            OR UPPER(COALESCE(nome, '')) LIKE '%RENDA IMOBILIARIA%'
            OR UPPER(COALESCE(nome, '')) LIKE '%GALPOES%'
            OR REGEXP_REPLACE(
                UPPER(
                    CASE
                        WHEN UPPER(COALESCE(ticker, '')) LIKE '%.SA'
                            THEN LEFT(UPPER(COALESCE(ticker, '')), LENGTH(UPPER(COALESCE(ticker, ''))) - 3)
                        ELSE UPPER(COALESCE(ticker, ''))
                    END
                ),
                '[^A-Z0-9]',
                ''
            ) REGEXP '11$'
        )
        THEN 'FII'

    ELSE tipo
END
WHERE tipo IN ('ACAO', 'ETF', 'FII', 'CRIPTOMOEDA', 'INDICE');
