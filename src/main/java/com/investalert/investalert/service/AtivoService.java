package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.AtivoRequestDTO;
import com.investalert.investalert.dto.response.AtivoResponseDTO;
import com.investalert.investalert.exception.BusinessException;
import com.investalert.investalert.exception.ResourceNotFoundException;
import com.investalert.investalert.model.Ativo;
import com.investalert.investalert.model.PrecoAtivo;
import com.investalert.investalert.repository.AtivoRepository;
import com.investalert.investalert.repository.PrecoAtivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtivoService {

    private final AtivoRepository ativoRepository;
    private final PrecoAtivoRepository precoAtivoRepository;

    @Transactional
    public AtivoResponseDTO cadastrar(AtivoRequestDTO dto) {
        if (ativoRepository.existsByTicker(dto.getTicker().toUpperCase())) {
            throw new BusinessException("Ativo já cadastrado com ticker: " + dto.getTicker());
        }

        Ativo ativo = Ativo.builder()
                .ticker(dto.getTicker().toUpperCase())
                .nome(dto.getNome())
                .tipo(dto.getTipo())
                .mercado(dto.getMercado())
                .build();

        return toResponse(ativoRepository.save(ativo), null);
    }

    @Transactional(readOnly = true)
    public AtivoResponseDTO buscarPorTicker(String ticker) {
        Ativo ativo = ativoRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Ativo", "ticker", ticker));

        BigDecimal precoAtual = buscarPrecoAtual(ativo.getId());
        return toResponse(ativo, precoAtual);
    }

    @Transactional(readOnly = true)
    public List<AtivoResponseDTO> listarTodos() {
        return ativoRepository.findAll().stream()
                .map(ativo -> toResponse(ativo, buscarPrecoAtual(ativo.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Ativo buscarEntidadePorTicker(String ticker) {
        return ativoRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Ativo", "ticker", ticker));
    }

    private BigDecimal buscarPrecoAtual(Long ativoId) {
        return precoAtivoRepository.findTopByAtivoIdOrderByDataHoraDesc(ativoId)
                .map(PrecoAtivo::getPreco)
                .orElse(null);
    }

    private AtivoResponseDTO toResponse(Ativo ativo, BigDecimal precoAtual) {
        return AtivoResponseDTO.builder()
                .id(ativo.getId())
                .ticker(ativo.getTicker())
                .nome(ativo.getNome())
                .tipo(ativo.getTipo())
                .mercado(ativo.getMercado())
                .precoAtual(precoAtual)
                .build();
    }
}