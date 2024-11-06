package com.pollub.cookiefx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductExportDTO {
    private String nazwa;
    private String opis;
    private BigDecimal cena;
    private BigDecimal gramatura;
    private Integer iloscNaStanie;
    @JsonProperty("kategorie")
    private List<Long> kategorieIds;

    @JsonProperty("zdjecie")
    private String zdjecieUrl;
}
