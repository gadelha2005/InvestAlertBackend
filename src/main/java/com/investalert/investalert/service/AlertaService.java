package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.AlertaRequestDTO;
import com.investalert.investalert.dto.response.AlertaResponseDTO;
import com.investalert.investalert.exception.ResourceNotFoundException;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.model.Alerta;
import com.investalert.investalert.model.Ativo;
import com.investalert.investalert.model.PrecoAtivo;
import com.investalert.investalert.model.Usuario;
import com.investalert.investalert.repository.AlertaRepository;
import com.investalert.investalert.repository.PrecoAtivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final PrecoAtivoRepository precoAtivoRepository;
    private final AtivoService ativoService;
    private final UsuarioService usuarioService;

    @Transactional
    public AlertaResponseDTO criar(Long usuarioId, AlertaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarEntidadePorEmail(
                usuarioService.buscarPorId(usuarioId).getEmail()
        );

        Ativo ativo = ativoService.buscarEntidadePorTicker(dto.getTicker());

        Alerta alerta = Alerta.builder()
                .usuario(usuario)
                .ativo(ativo)
                .tipo(dto.getTipo())
                .valorAlvo(dto.getValorAlvo())
                .notificarWhatsapp(dto.getNotificarWhatsapp())
                .build();

        Alerta salvo = alertaRepository.save(alerta);
        BigDecimal precoAtual = buscarPrecoAtual(ativo.getId());

        return toResponse(salvo, precoAtual);
    }

    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> listarPorUsuario(Long usuarioId) {
        return alertaRepository.findByUsuarioId(usuarioId).stream()
                .map(alerta -> toResponse(alerta, buscarPrecoAtual(alerta.getAtivo().getId())))
                .toList();
    }

    @Transactional
    public void deletar(Long alertaId, Long usuarioId) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", alertaId));

        if (!alerta.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Sem permissão para deletar este alerta");
        }

        alertaRepository.delete(alerta);
    }

    @Transactional
    public AlertaResponseDTO alterarStatus(Long alertaId, Long usuarioId, Boolean ativado) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", alertaId));

        if (!alerta.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Sem permissão para alterar este alerta");
        }

        alerta.setAtivado(ativado);
        BigDecimal precoAtual = buscarPrecoAtual(alerta.getAtivo().getId());

        return toResponse(alertaRepository.save(alerta), precoAtual);
    }

    private BigDecimal buscarPrecoAtual(Long ativoId) {
        return precoAtivoRepository.findTopByAtivoIdOrderByDataHoraDesc(ativoId)
                .map(PrecoAtivo::getPreco)
                .orElse(null);
    }

    private AlertaResponseDTO toResponse(Alerta alerta, BigDecimal precoAtual) {
        return AlertaResponseDTO.builder()
                .id(alerta.getId())
                .ticker(alerta.getAtivo().getTicker())
                .tipo(alerta.getTipo())
                .valorAlvo(alerta.getValorAlvo())
                .precoAtual(precoAtual)
                .notificarWhatsapp(alerta.getNotificarWhatsapp())
                .ativado(alerta.getAtivado())
                .dataCriacao(alerta.getDataCriacao())
                .build();
    }
}