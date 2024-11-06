package com.pollub.cookiefx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Data
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String nazwa;
    private String opis;
    private BigDecimal cena;
    private BigDecimal gramatura;
    private Integer iloscNaStanie;
    @JsonProperty("kategorie")
    private List<CategoryDTO> kategorieIds;

    @JsonProperty("zdjecie")
    private String zdjecieUrl;


}
