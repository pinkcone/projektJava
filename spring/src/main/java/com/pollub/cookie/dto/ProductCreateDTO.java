package com.pollub.cookie.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

import lombok.Data;


@Data
public class ProductCreateDTO {

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

    private List<Long> kategorieIds;

}
