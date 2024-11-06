package com.pollub.cookiefx.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Data
@NoArgsConstructor
@Getter
public class ProductCreateDTO {
    private String nazwa;
    private String opis;
    private BigDecimal cena;
    private BigDecimal gramatura;
    private Integer iloscNaStanie;
    private List<Long> kategorieIds;



    public ProductCreateDTO(String nazwa, String opis, BigDecimal cena, BigDecimal gramatura, Integer iloscNaStanie, List<Long> kategorieIds) {
        this.nazwa = nazwa;
        this.opis = opis;
        this.cena = cena;
        this.gramatura = gramatura;
        this.iloscNaStanie = iloscNaStanie;
        this.kategorieIds = kategorieIds;
    }

}
