package com.pollub.cookie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
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
    @JsonProperty("kategorie")
    private List<Long> kategorieIds;
    @JsonProperty("zdjecie")
    private String zdjecieUrl;

}


