package com.investalert.investalert.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinGeckoMarketChartDTO {

    private List<List<Double>> prices;

    @JsonProperty("total_volumes")
    private List<List<Double>> totalVolumes;
}
