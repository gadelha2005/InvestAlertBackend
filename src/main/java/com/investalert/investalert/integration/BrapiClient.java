package com.investalert.investalert.integration;

import com.investalert.investalert.integration.dto.BrapiAssetInfoDTO;
import com.investalert.investalert.integration.dto.BrapiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrapiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${integration.brapi.base-url}")
    private String baseUrl;

    @Value("${integration.brapi.token:}")
    private String token;

    public Optional<BigDecimal> buscarPreco(String ticker, String mercado) {
        for (String tickerConsulta : montarTickersConsulta(ticker, mercado)) {
            Optional<BigDecimal> preco = buscarPrecoPorTicker(tickerConsulta);
            if (preco.isPresent()) {
                return preco;
            }
        }

        return Optional.empty();
    }

    public Optional<BrapiAssetInfoDTO> buscarDetalhesAtivo(String ticker) {
        for (String tickerConsulta : montarTickersConsultaSemMercado(ticker)) {
            Optional<BrapiAssetInfoDTO> detalhes = buscarDetalhesPorTicker(tickerConsulta);
            if (detalhes.isPresent()) {
                return detalhes;
            }
        }

        return Optional.empty();
    }

    public Optional<QuoteSnapshot> buscarResumoMercado(String ticker, String mercado) {
        for (String tickerConsulta : montarTickersConsulta(ticker, mercado)) {
            Optional<BrapiResponseDTO.Result> resultado = buscarPrimeiroResultado(tickerConsulta);
            if (resultado.isPresent()) {
                BrapiResponseDTO.Result item = resultado.get();
                BigDecimal preco = item.getRegularMarketPrice() != null
                        ? BigDecimal.valueOf(item.getRegularMarketPrice())
                        : null;
                BigDecimal variacaoPercentual = item.getRegularMarketChangePercent() != null
                        ? BigDecimal.valueOf(item.getRegularMarketChangePercent())
                        : null;

                return Optional.of(new QuoteSnapshot(
                        preco,
                        variacaoPercentual,
                        item.getRegularMarketVolume()
                ));
            }
        }

        return Optional.empty();
    }

    private Optional<BigDecimal> buscarPrecoPorTicker(String ticker) {
        Optional<BrapiResponseDTO.Result> resultado = buscarPrimeiroResultado(ticker);
        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        Double preco = resultado.get().getRegularMarketPrice();
        if (preco == null) {
            log.warn("Preco nulo retornado pela Brapi para ticker: {}", ticker);
            return Optional.empty();
        }

        log.debug("Preco obtido da Brapi para {}: {}", ticker, preco);
        return Optional.of(BigDecimal.valueOf(preco));
    }

    private Optional<BrapiAssetInfoDTO> buscarDetalhesPorTicker(String ticker) {
        Optional<BrapiResponseDTO.Result> resultado = buscarPrimeiroResultado(ticker);
        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        BrapiResponseDTO.Result item = resultado.get();
        String nome = item.getLongName() != null && !item.getLongName().isBlank()
                ? item.getLongName()
                : item.getShortName();

        return Optional.of(BrapiAssetInfoDTO.builder()
                .tickerConsultado(ticker)
                .simboloRetornado(item.getSymbol())
                .nome(nome)
                .quoteType(item.getQuoteType())
                .exchange(item.getExchange())
                .currency(item.getCurrency())
                .build());
    }

    private Optional<BrapiResponseDTO.Result> buscarPrimeiroResultado(String ticker) {
        try {
            WebClient.RequestHeadersSpec<?> request = webClientBuilder
                    .baseUrl(baseUrl)
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/quote/{ticker}")
                            .queryParam("fundamental", "false")
                            .queryParamIfPresent("token", obterTokenConfigurado())
                            .build(ticker))
                    .headers(headers -> adicionarAutorizacao(headers, token));

            BrapiResponseDTO response = request.retrieve()
                    .bodyToMono(BrapiResponseDTO.class)
                    .block();

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                log.warn("Nenhum resultado retornado pela Brapi para ticker: {}", ticker);
                return Optional.empty();
            }

            return Optional.of(response.getResults().get(0));
        } catch (WebClientResponseException.Unauthorized e) {
            log.error("Brapi recusou a autenticacao para ticker {}. Verifique o token configurado.", ticker);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro ao consultar a Brapi para ticker {}: {}", ticker, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> obterTokenConfigurado() {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(token.trim());
    }

    private void adicionarAutorizacao(HttpHeaders headers, String tokenConfigurado) {
        if (tokenConfigurado == null || tokenConfigurado.isBlank()) {
            return;
        }

        headers.setBearerAuth(tokenConfigurado.trim());
    }

    private List<String> montarTickersConsulta(String ticker, String mercado) {
        List<String> tickers = new ArrayList<>();
        String tickerNormalizado = ticker.toUpperCase(Locale.ROOT);
        tickers.add(tickerNormalizado);

        if (deveTentarTickerB3(tickerNormalizado, mercado)) {
            tickers.add(tickerNormalizado + ".SA");
        }

        return tickers;
    }

    private List<String> montarTickersConsultaSemMercado(String ticker) {
        List<String> tickers = new ArrayList<>();
        String tickerNormalizado = ticker.toUpperCase(Locale.ROOT);
        tickers.add(tickerNormalizado);

        if (!tickerNormalizado.contains(".")) {
            tickers.add(tickerNormalizado + ".SA");
        }

        return tickers;
    }

    private boolean deveTentarTickerB3(String ticker, String mercado) {
        if (ticker.contains(".")) {
            return false;
        }

        if (mercado == null || mercado.isBlank()) {
            return false;
        }

        String mercadoNormalizado = mercado.trim().toUpperCase(Locale.ROOT);
        return mercadoNormalizado.equals("B3")
                || mercadoNormalizado.equals("BR")
                || mercadoNormalizado.equals("BRASIL")
                || mercadoNormalizado.equals("BRAZIL");
    }

    public record QuoteSnapshot(
            BigDecimal precoAtual,
            BigDecimal variacaoPercentual,
            Long volume
    ) {
    }
}
