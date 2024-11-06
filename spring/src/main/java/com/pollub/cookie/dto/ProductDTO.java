package com.pollub.cookie.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {

    @Setter
    private Long id;

    @NotBlank(message = "Nazwa produktu jest wymagana")
    private String nazwa;

    @NotBlank(message = "Opis produktu jest wymagany")
    private String opis;

    @DecimalMin(value = "0.0", inclusive = false, message = "Cena musi być większa od zera")
    private BigDecimal gramatura;


    private String zdjecie;

    @NotNull(message = "Ilość na stanie jest wymagana")
    @Positive(message = "Ilość na stanie musi być dodatnia")
    private Integer iloscNaStanie;

    @NotNull(message = "Cena produktu jest wymagana")
    @Positive(message = "Cena musi być dodatnia")
    private BigDecimal cena;

    private List<CategoryDTO> kategorie;

}
