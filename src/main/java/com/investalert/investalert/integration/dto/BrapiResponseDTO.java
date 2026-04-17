package com.investalert.investalert.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrapiResponseDTO {

    private List<Result> results;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String symbol;
        private String longName;
        private String shortName;
        private String quoteType;
        private String exchange;
        private String currency;
        private Double regularMarketPrice;
        private Double regularMarketChangePercent;
        private Long regularMarketVolume;
        private List<HistoricalDataPoint> historicalDataPrice;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoricalDataPoint {
        private Long date;
        private Double open;
        private Double high;
        private Double low;
        private Double close;
        private Long volume;
        private Double adjustedClose;
    }
}
