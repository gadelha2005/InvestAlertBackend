package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.AtivoRequestDTO;
import com.investalert.investalert.dto.response.AtivoResponseDTO;
import com.investalert.investalert.exception.BusinessException;
import com.investalert.investalert.exception.ResourceNotFoundException;
import com.investalert.investalert.integration.PrecoService;
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
    private final PrecoService precoService;

    @Transactional
    public AtivoResponseDTO cadastrar(AtivoRequestDTO dto) {
        String tickerNormalizado = dto.getTicker().toUpperCase();

        if (ativoRepository.existsByTicker(tickerNormalizado)) {
            throw new BusinessException("Ativo ja cadastrado com ticker: " + dto.getTicker());
        }

        Ativo ativo = Ativo.builder()
                .ticker(tickerNormalizado)
                .nome(dto.getNome())
                .tipo(dto.getTipo())
                .mercado(dto.getMercado())
                .build();

        Ativo ativoSalvo = ativoRepository.save(ativo);
        BigDecimal precoAtual = precoService.atualizarPreco(ativoSalvo)
                .orElseThrow(() -> new BusinessException(
                        "Nao foi possivel obter o preco atual para o ativo " + tickerNormalizado
                                + ". Verifique se o ticker esta correto e disponivel na fonte de cotacao."
                ));

        return toResponse(ativoSalvo, precoAtual);
    }

    @Transactional(readOnly = true)
    public AtivoResponseDTO buscarPorTicker(String ticker) {
        Ativo ativo = ativoRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Ativo", "ticker", ticker));

        BigDecimal precoAtual = buscarPrecoAtualOuAtualizar(ativo);
        return toResponse(ativo, precoAtual);
    }

    @Transactional(readOnly = true)
    public List<AtivoResponseDTO> listarTodos() {
        return ativoRepository.findAll().stream()
                .map(ativo -> toResponse(ativo, buscarPrecoAtualOuAtualizar(ativo)))
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

    private BigDecimal buscarPrecoAtualOuAtualizar(Ativo ativo) {
        BigDecimal precoAtual = buscarPrecoAtual(ativo.getId());
        if (precoAtual != null) {
            return precoAtual;
        }

        return precoService.atualizarPreco(ativo).orElse(null);
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
