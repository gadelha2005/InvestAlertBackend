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
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
                .tipo(dto.getTipo())
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

        return toResponse(ativoSalvo, precoAtual, true);
    }

    @Transactional(readOnly = true)
    public AtivoResponseDTO buscarPorTicker(String ticker) {
        Ativo ativo = ativoRepository.findByTicker(ticker.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Ativo", "ticker", ticker));

        BigDecimal precoAtual = buscarPrecoAtualOuAtualizar(ativo);
        return toResponse(ativo, precoAtual, true);
    }

    @Transactional(readOnly = true)
    public List<AtivoResponseDTO> listarTodos() {
        return ativoRepository.findAll().stream()
                .map(ativo -> toResponse(ativo, buscarPrecoAtual(ativo.getId()), false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AtivoResponseDTO> listarParaScanner() {
        List<Ativo> ativos = ativoRepository.findAll();
        
        if (ativos.isEmpty()) {
            return List.of();
        }

        // Buscar todos os IDs dos ativos
        List<Long> ativoIds = ativos.stream()
                .map(Ativo::getId)
                .toList();

        // Buscar todos os preços recentes em uma única query
        Map<Long, PrecoAtivo> precosPorAtivoId = precoAtivoRepository
                .findLatestPricesByAtivoIds(ativoIds).stream()
                .collect(Collectors.toMap(
                        pa -> pa.getAtivo().getId(),
                        pa -> pa
                ));

        // Buscar histórico recente (últimos 2 preços) para calcular variação
        Map<Long, List<PrecoAtivo>> historicoPorAtivoId = ativos.stream()
                .collect(Collectors.toMap(
                        Ativo::getId,
                        ativo -> precoAtivoRepository.findTop2ByAtivoIdOrderByDataHoraDesc(ativo.getId())
                ));

        // Mapear para resposta sem consultarResumoMercado
        return ativos.stream()
                .map(ativo -> {
                    BigDecimal precoAtual = precosPorAtivoId.containsKey(ativo.getId()) 
                            ? precosPorAtivoId.get(ativo.getId()).getPreco()
                            : buscarPrecoAtual(ativo.getId());
                    List<PrecoAtivo> historico = historicoPorAtivoId.get(ativo.getId());
                    return toResponseScannerOtimizado(ativo, precoAtual, historico);
                })
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
        String tickerConsultado = normalizar(detalhes.getTickerConsultado());

        if (ehCriptomoeda(quoteType, exchange, simbolo, tickerConsultado)) {
            return TipoAtivo.CRIPTOMOEDA;
        }

        if (ehIndice(quoteType, simbolo, tickerConsultado, nome)) {
            return TipoAtivo.INDICE;
        }

        if (ehEtf(quoteType, exchange, nome, simbolo, tickerConsultado)) {
            return TipoAtivo.ETF;
        }

        if (ehFii(exchange, nome, simbolo, quoteType)) {
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

    private boolean ehCriptomoeda(String quoteType, String exchange, String simbolo, String tickerConsultado) {
        return "CRYPTOCURRENCY".equals(quoteType)
                || "CRYPTO".equals(quoteType)
                || exchange.contains("CRYPTO")
                || simbolo.endsWith("-USD")
                || tickerConsultado.endsWith("-USD");
    }

    private boolean ehIndice(String quoteType, String simbolo, String tickerConsultado, String nome) {
        return "INDEX".equals(quoteType)
                || "INDICE".equals(quoteType)
                || simbolo.startsWith("^")
                || tickerConsultado.startsWith("^")
                || nome.startsWith("INDICE ")
                || nome.contains(" IBOV")
                || nome.contains(" S&P ")
                || nome.contains(" NASDAQ")
                || nome.contains(" DOW JONES");
    }

    private boolean ehEtf(String quoteType,
                          String exchange,
                          String nome,
                          String simbolo,
                          String tickerConsultado) {
        if ("ETF".equals(quoteType)) {
            return true;
        }

        if (nome.contains("ETF")
                || nome.contains("EXCHANGE TRADED FUND")
                || nome.contains("ISHARES")
                || nome.contains("INDEX FUND")
                || nome.contains("INDICE ETF")) {
            return true;
        }

        if (!ehMercadoBrasileiro(exchange, simbolo)) {
            return false;
        }

        String tickerBase = removerSufixoBolsa(simbolo.isBlank() ? tickerConsultado : simbolo);
        return tickerBase.endsWith("11")
                && !ehPossivelFiiPorNome(nome)
                && !tickerBase.matches(".*(CI|CR|TA|TG|HG|XP|KN|BR|RB|VR|TR)$");
    }

    private boolean ehFii(String exchange, String nome, String simbolo, String quoteType) {
        if (!ehMercadoBrasileiro(exchange, simbolo)) {
            return false;
        }

        if ("REIT".equals(quoteType)) {
            return true;
        }

        return nome.contains("FII")
                || nome.contains("FDO IMOB")
                || nome.contains("FUNDO IMOB")
                || nome.contains("FUNDO DE INVESTIMENTO IMOBILIARIO")
                || nome.contains("LOGISTICA")
                || nome.contains("LAJES")
                || nome.contains("SHOPPING")
                || nome.contains("RENDA IMOBILIARIA")
                || nome.contains("GALPOES")
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

    private boolean ehPossivelFiiPorNome(String nome) {
        return nome.contains("FII")
                || nome.contains("FDO IMOB")
                || nome.contains("FUNDO IMOB")
                || nome.contains("FUNDO DE INVESTIMENTO IMOBILIARIO")
                || nome.contains("LOGISTICA")
                || nome.contains("SHOPPING")
                || nome.contains("RENDA IMOBILIARIA")
                || nome.contains("GALPOES")
                || nome.contains("LAJES");
    }

    private String removerSufixoBolsa(String simbolo) {
        if (simbolo.endsWith(".SA")) {
            return simbolo.substring(0, simbolo.length() - 3);
        }

        return simbolo;
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

    private List<PrecoAtivo> buscarHistoricoRecente(Long ativoId) {
        return precoAtivoRepository.findTop2ByAtivoIdOrderByDataHoraDesc(ativoId);
    }

    private BigDecimal buscarPrecoAtualOuAtualizar(Ativo ativo) {
        BigDecimal precoAtual = buscarPrecoAtual(ativo.getId());
        if (precoAtual != null) {
            return precoAtual;
        }

        return precoService.atualizarPreco(ativo).orElse(null);
    }

    private AtivoResponseDTO toResponse(Ativo ativo, BigDecimal precoAtual, boolean consultarResumoMercado) {
        List<PrecoAtivo> historicoRecente = buscarHistoricoRecente(ativo.getId());
        BrapiClient.QuoteSnapshot resumoMercado = consultarResumoMercado
                ? brapiClient.buscarResumoMercado(ativo.getTicker(), ativo.getMercado()).orElse(null)
                : null;

        BigDecimal precoAtualResolvido = resumoMercado != null && resumoMercado.precoAtual() != null
                ? resumoMercado.precoAtual()
                : precoAtual;
        BigDecimal variacaoPercentual = resumoMercado != null
                ? resumoMercado.variacaoPercentual()
                : null;

        if (variacaoPercentual == null) {
            variacaoPercentual = calcularVariacaoPercentual(historicoRecente, precoAtualResolvido);
        }

        BigDecimal variacao = calcularVariacaoAbsoluta(historicoRecente, precoAtualResolvido, variacaoPercentual);

        return AtivoResponseDTO.builder()
                .id(ativo.getId())
                .ticker(ativo.getTicker())
                .nome(ativo.getNome())
                .tipo(ativo.getTipo())
                .mercado(ativo.getMercado())
                .precoAtual(precoAtualResolvido)
                .variacao(variacao)
                .variacaoPercentual(variacaoPercentual)
                .volume(resumoMercado != null ? resumoMercado.volume() : null)
                .build();
    }

    private AtivoResponseDTO toResponseScannerOtimizado(Ativo ativo, BigDecimal precoAtual, List<PrecoAtivo> historicoRecente) {
        // Versão otimizada para scanner sem chamadas a resumoMercado
        BigDecimal variacaoPercentual = calcularVariacaoPercentual(historicoRecente, precoAtual);
        BigDecimal variacao = calcularVariacaoAbsoluta(historicoRecente, precoAtual, variacaoPercentual);

        return AtivoResponseDTO.builder()
                .id(ativo.getId())
                .ticker(ativo.getTicker())
                .nome(ativo.getNome())
                .tipo(ativo.getTipo())
                .mercado(ativo.getMercado())
                .precoAtual(precoAtual)
                .variacao(variacao)
                .variacaoPercentual(variacaoPercentual)
                .volume(null) // Não consultarmos volume no scanner para melhor performance
                .build();
    }

    private BigDecimal calcularVariacaoPercentual(List<PrecoAtivo> historicoRecente, BigDecimal precoAtual) {
        if (precoAtual == null || historicoRecente.size() < 2) {
            return null;
        }

        BigDecimal precoAnterior = historicoRecente.get(1).getPreco();
        if (precoAnterior == null || precoAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return precoAtual.subtract(precoAnterior)
                .divide(precoAnterior, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calcularVariacaoAbsoluta(List<PrecoAtivo> historicoRecente,
                                                BigDecimal precoAtual,
                                                BigDecimal variacaoPercentual) {
        if (precoAtual == null) {
            return null;
        }

        if (historicoRecente.size() >= 2 && historicoRecente.get(1).getPreco() != null) {
            return precoAtual.subtract(historicoRecente.get(1).getPreco());
        }

        if (variacaoPercentual == null) {
            return null;
        }

        BigDecimal fator = BigDecimal.ONE.add(
                variacaoPercentual.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );

        if (fator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal precoAnteriorEstimado = precoAtual.divide(fator, 6, RoundingMode.HALF_UP);
        return precoAtual.subtract(precoAnteriorEstimado);
    }
}

