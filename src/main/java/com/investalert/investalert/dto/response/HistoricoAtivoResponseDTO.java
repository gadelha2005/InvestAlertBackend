package com.investalert.investalert.dto.response;

import com.investalert.investalert.integration.dto.PontoHistoricoDTO;
import com.investalert.investalert.model.enums.TipoAtivo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HistoricoAtivoResponseDTO {
    private String ticker;
    private String nome;
    private TipoAtivo tipo;
    private String periodo;
    private List<PontoHistoricoDTO> dados;
    private List<DiaResumoDTO> ultimos7Dias;
}
