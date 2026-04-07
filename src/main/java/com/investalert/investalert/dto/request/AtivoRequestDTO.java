package com.investalert.investalert.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtivoRequestDTO {

    @NotBlank(message = "Ticker e obrigatorio")
    @Size(max = 10, message = "Ticker deve ter no maximo 10 caracteres")
    private String ticker;
}
