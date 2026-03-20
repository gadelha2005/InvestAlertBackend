package com.investalert.investalert.dto.request;

import com.investalert.investalert.model.enums.TipoAtivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtivoRequestDTO {

    @NotBlank(message = "Ticker é obrigatório")
    @Size(max = 10, message = "Ticker deve ter no máximo 10 caracteres")
    private String ticker;

    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotNull(message = "Tipo é obrigatório")
    private TipoAtivo tipo;

    @Size(max = 50, message = "Mercado deve ter no máximo 50 caracteres")
    private String mercado;
}