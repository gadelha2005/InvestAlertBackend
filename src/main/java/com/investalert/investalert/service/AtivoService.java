package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.AtivoRequestDTO;
import com.investalert.investalert.dto.response.AtivoResponseDTO;
import com.investalert.investalert.exception.BusinessException;
import com.investalert.investalert.exception.ResourceNotFoundException;
import com.investalert.investalert.integration.BrapiClient;
import com.investalert.investalert.integration.PrecoService;
import com.investalert.investalert.integration.dto.BrapiAssetInfoDTO;
import com.investalert.investalert.model.Ativo;
import com.investalert.investalert.model.PrecoAtivo;
import com.investalert.investalert.model.enums.TipoAtivo;
import com.investalert.investalert.repository.AtivoRepository;
import com.investalert.investalert.repository.PrecoAtivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AtivoService {

    private final AtivoRepository ativoRepository;
    private final PrecoAtivoRepository precoAtivoRepository;
    private final PrecoService precoService;
    private final BrapiClient brapiClient;

    @Transactional
    public AtivoResponseDTO cadastrar(AtivoRequestDTO dto) {
        String tickerNormalizado = dto.getTicker().trim().toUpperCase(Locale.ROOT);

        if (ativoRepository.existsByTicker(tickerNormalizado)) {
            throw new BusinessException("Ativo ja cadastrado com ticker: " + dto.getTicker());
        }

        BrapiAssetInfoDTO detalhes = brapiClient.buscarDetalhesAtivo(tickerNormalizado)
                .orElseThrow(() -> new BusinessException(
                        "Nao foi possivel localizar o ativo " + tickerNormalizado
                                + " na Brapi. Verifique se o ticker esta correto."
                ));

        Ativo ativo = Ativo.builder()
                .ticker(resolverTickerPersistido(detalhes, tickerNormalizado))
                .nome(resolverNomeAtivo(detalhes, tickerNormalizado))
                .tipo(mapearTipoAtivo(detalhes))
                .mercado(resolverMercado(detalhes))
                .build();

        if (ativoRepository.existsByTicker(ativo.getTicker())) {
            throw new BusinessException("Ativo ja cadastrado com ticker: " + ativo.getTicker());
        }

        Ativo ativoSalvo = ativoRepository.save(ativo);
        BigDecimal precoAtual = precoService.atualizarPreco(ativoSalvo)
                .orElseThrow(() -> new BusinessException(
                        "Nao foi possivel obter o preco atual para o ativo " + ativo.getTicker()
                                + ". Verifique se o ticker esta correto e disponivel na fonte de cotacao."
                ));

        return toResponse(ativoSalvo, precoAtual);
    }

    @Transactional(readOnly = true)
    public AtivoResponseDTO buscarPorTicker(String ticker) {
        Ativo ativo = ativoRepository.findByTicker(ticker.toUpperCase(Locale.ROOT))
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
        return ativoRepository.findByTicker(ticker.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Ativo", "ticker", ticker));
    }

    private TipoAtivo mapearTipoAtivo(BrapiAssetInfoDTO detalhes) {
        String quoteType = normalizar(detalhes.getQuoteType());
        String exchange = normalizar(detalhes.getExchange());
        String nome = normalizar(detalhes.getNome());
        String simbolo = normalizar(detalhes.getSimboloRetornado());

        if ("CRYPTOCURRENCY".equals(quoteType)) {
            return TipoAtivo.CRIPTOMOEDA;
        }

        if ("ETF".equals(quoteType)) {
            return TipoAtivo.ETF;
        }

        if ("INDEX".equals(quoteType) || "INDICE".equals(quoteType)) {
            return TipoAtivo.INDICE;
        }

        if (ehFii(exchange, nome, simbolo)) {
            return TipoAtivo.FII;
        }

        if ("EQUITY".equals(quoteType)
                || "STOCK".equals(quoteType)
                || "MUTUALFUND".equals(quoteType)
                || "FUND".equals(quoteType)) {
            return TipoAtivo.ACAO;
        }

        if (pareceAtivoListado(exchange, simbolo, nome)) {
            return TipoAtivo.ACAO;
        }

        throw new BusinessException("Nao foi possivel identificar o tipo do ativo para o ticker informado.");
    }

    private boolean ehFii(String exchange, String nome, String simbolo) {
        if (!ehMercadoBrasileiro(exchange, simbolo)) {
            return false;
        }

        return nome.contains("FII")
                || nome.contains("FDO IMOB")
                || nome.contains("FUNDO IMOB")
                || nome.contains("FUNDO DE INVESTIMENTO IMOBILIARIO")
                || simbolo.endsWith("11");
    }

    private boolean pareceAtivoListado(String exchange, String simbolo, String nome) {
        if (ehMercadoBrasileiro(exchange, simbolo)) {
            return true;
        }

        return !exchange.isBlank()
                || simbolo.matches("^[A-Z0-9.\\-]{3,15}$")
                || !nome.isBlank();
    }

    private boolean ehMercadoBrasileiro(String exchange, String simbolo) {
        return "B3".equals(exchange)
                || "BVMF".equals(exchange)
                || "BVSP".equals(exchange)
                || "SAO".equals(exchange)
                || "SAOPAULO".equals(exchange)
                || simbolo.endsWith(".SA");
    }

    private String resolverNomeAtivo(BrapiAssetInfoDTO detalhes, String tickerFallback) {
        if (detalhes.getNome() == null || detalhes.getNome().isBlank()) {
            return tickerFallback;
        }

        return detalhes.getNome().trim();
    }

    private String resolverMercado(BrapiAssetInfoDTO detalhes) {
        if (detalhes.getExchange() != null && !detalhes.getExchange().isBlank()) {
            return detalhes.getExchange().trim();
        }

        if (detalhes.getCurrency() != null && !detalhes.getCurrency().isBlank()) {
            return detalhes.getCurrency().trim();
        }

        return null;
    }

    private String resolverTickerPersistido(BrapiAssetInfoDTO detalhes, String tickerFallback) {
        String simbolo = detalhes.getSimboloRetornado();
        if (simbolo == null || simbolo.isBlank()) {
            return tickerFallback;
        }

        return simbolo.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim().toUpperCase(Locale.ROOT);
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
