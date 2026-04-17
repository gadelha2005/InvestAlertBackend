package com.investalert.investalert.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DiaResumoDTO {
    private String data;
    private BigDecimal preco;
    private BigDecimal variacaoPercentual;
    private Long volume;
}
