package com.investalert.investalert.integration.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrapiAssetInfoDTO {

    private final String tickerConsultado;
    private final String simboloRetornado;
    private final String nome;
    private final String quoteType;
    private final String exchange;
    private final String currency;
}
