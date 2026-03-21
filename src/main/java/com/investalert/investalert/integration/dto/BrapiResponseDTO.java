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
        private String shortName;
        private Double regularMarketPrice;
        private Double regularMarketChangePercent;
    }
}