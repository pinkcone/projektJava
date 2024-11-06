package com.pollub.cookie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pollub.cookie.model.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor

public class ProductImportJsonDTO {


    @NotBlank(message = "Nazwa produktu jest wymagana")
    private String nazwa;

    private String opis;

    @NotNull(message = "Cena jest wymagana")
    @DecimalMin(value = "0.0", inclusive = false, message = "Cena musi być większa od zera")
    private BigDecimal cena;

    @DecimalMin(value = "0.0", inclusive = false, message = "Gramatura musi być większa od zera")
    private BigDecimal gramatura;

    @Min(value = 0, message = "Ilość na stanie nie może być ujemna")
    private Integer iloscNaStanie;
    @JsonDeserialize(as = ArrayList.class)
    @JsonProperty("kategorie")
    private List<Long> kategorieIds = new ArrayList<>();
    @JsonProperty("zdjecie")
    private String zdjecieUrl;
    public ProductImportJsonDTO(Product product) {}

    public ProductImportJsonDTO(String nazwa, String opis, BigDecimal cena, BigDecimal gramatura, Integer iloscNaStanie, List<Long> kategorieIds, String zdjecieUrl) {
        this.nazwa = nazwa;
        this.opis = opis;
        this.cena = cena;
        this.gramatura = gramatura;
        this.iloscNaStanie = iloscNaStanie;
        this.kategorieIds = kategorieIds;
        this.zdjecieUrl = zdjecieUrl;
    }
}


