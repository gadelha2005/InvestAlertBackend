package com.investalert.investalert.integration;

import com.investalert.investalert.integration.dto.CoinGeckoResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
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