package com.investalert.investalert.integration;

import com.investalert.investalert.integration.dto.BrapiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrapiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${integration.brapi.base-url}")
    private String baseUrl;

    public Optional<BigDecimal> buscarPreco(String ticker) {
        try {
            BrapiResponseDTO response = webClientBuilder
                    .baseUrl(baseUrl)
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/quote/{ticker}")
                            .queryParam("fundamental", "false")
                            .build(ticker))
                    .retrieve()
                    .bodyToMono(BrapiResponseDTO.class)
                    .block();

            if (response == null
                    || response.getResults() == null
                    || response.getResults().isEmpty()) {
                log.warn("Nenhum resultado retornado pela Brapi para ticker: {}", ticker);
                return Optional.empty();
            }

            Double preco = response.getResults().get(0).getRegularMarketPrice();

            if (preco == null) {
                log.warn("Preço nulo retornado pela Brapi para ticker: {}", ticker);
                return Optional.empty();
            }

            log.debug("Preço obtido da Brapi para {}: {}", ticker, preco);
            return Optional.of(BigDecimal.valueOf(preco));

        } catch (Exception e) {
            log.error("Erro ao buscar preço na Brapi para ticker {}: {}", ticker, e.getMessage());
            return Optional.empty();
        }
    }
}