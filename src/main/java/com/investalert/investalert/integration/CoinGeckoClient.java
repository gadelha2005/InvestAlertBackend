package com.investalert.investalert.integration;

import com.investalert.investalert.integration.dto.CoinGeckoMarketChartDTO;
import com.investalert.investalert.integration.dto.CoinGeckoResponseDTO;
import com.investalert.investalert.integration.dto.PontoHistoricoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoinGeckoClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${integration.coingecko.base-url}")
    private String baseUrl;

    private static final Map<String, String> TICKER_TO_ID = Map.of(
            "BTC", "bitcoin",
            "ETH", "ethereum",
            "BNB", "binancecoin",
            "SOL", "solana",
            "ADA", "cardano",
            "DOT", "polkadot",
            "DOGE", "dogecoin",
            "MATIC", "matic-network",
            "LINK", "chainlink",
            "LTC", "litecoin"
    );

    public List<PontoHistoricoDTO> buscarHistoricoCrypto(String ticker, int dias) {
        try {
            String coinId = TICKER_TO_ID.getOrDefault(ticker.toUpperCase(), ticker.toLowerCase());

            CoinGeckoMarketChartDTO response = webClientBuilder
                    .baseUrl(baseUrl)
                    .build()
                    .get()
                    .uri("/coins/{id}/market_chart?vs_currency=brl&days={dias}", coinId, dias)
                    .retrieve()
                    .bodyToMono(CoinGeckoMarketChartDTO.class)
                    .block();

            if (response == null || response.getPrices() == null || response.getPrices().isEmpty()) {
                return Collections.emptyList();
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    .withZone(ZoneId.of("America/Sao_Paulo"));

            return response.getPrices().stream()
                    .filter(p -> p.size() >= 2 && p.get(1) != null)
                    .map(p -> new PontoHistoricoDTO(
                            fmt.format(Instant.ofEpochMilli(p.get(0).longValue())),
                            BigDecimal.valueOf(p.get(1))
                    ))
                    .toList();

        } catch (Exception e) {
            log.error("Erro ao buscar historico CoinGecko para {}: {}", ticker, e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<BigDecimal> buscarPreco(String ticker) {
        try {
            String coinId = TICKER_TO_ID.getOrDefault(ticker.toUpperCase(), ticker.toLowerCase());

            CoinGeckoResponseDTO response = webClientBuilder
                    .baseUrl(baseUrl)
                    .build()
                    .get()
                    .uri("/coins/{id}?localization=false&tickers=false&community_data=false&developer_data=false", coinId)
                    .retrieve()
                    .bodyToMono(CoinGeckoResponseDTO.class)
                    .block();

            if (response == null
                    || response.getMarketData() == null
                    || response.getMarketData().getCurrentPrice() == null
                    || response.getMarketData().getCurrentPrice().getBrl() == null) {
                log.warn("Nenhum resultado retornado pelo CoinGecko para ticker: {}", ticker);
                return Optional.empty();
            }

            return Optional.of(BigDecimal.valueOf(response.getMarketData().getCurrentPrice().getBrl()));

        } catch (Exception e) {
            log.error("Erro ao buscar preço no CoinGecko para ticker {}: {}", ticker, e.getMessage());
            return Optional.empty();
        }
    }
}