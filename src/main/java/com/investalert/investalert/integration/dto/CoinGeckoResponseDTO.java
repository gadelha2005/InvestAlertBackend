package com.investalert.investalert.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinGeckoResponseDTO {

    private String id;
    private String symbol;
    private String name;

    @JsonProperty("market_data")
    private MarketData marketData;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MarketData {

        @JsonProperty("current_price")
        private CurrentPrice currentPrice;

        @JsonProperty("price_change_percentage_24h")
        private Double priceChangePercentage24h;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentPrice {
        private Double brl;
    }
}