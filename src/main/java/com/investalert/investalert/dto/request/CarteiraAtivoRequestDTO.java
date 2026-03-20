package com.investalert.investalert.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CarteiraAtivoRequestDTO {

    @NotBlank(message = "Ticker é obrigatório")
    private String ticker;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.00000001", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    @NotNull(message = "Preço médio é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço médio deve ser maior que zero")
    private BigDecimal precoMedio;
}